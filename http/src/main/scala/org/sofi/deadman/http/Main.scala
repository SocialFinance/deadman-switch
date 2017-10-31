package org.sofi.deadman.http

import akka.actor._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.sofi.deadman.http.api._
import org.sofi.deadman.http.server._
import org.sofi.deadman.http.router._
import org.sofi.deadman.location.NetworkLocation
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._, QueryType._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.io.Source
import scala.util.Try

object Main extends App with Server {

  // Read args
  val id = Try(args(0)).getOrElse("")
  val mode = Try(args(1)).getOrElse("")

  // Boot actor system
  val config = if (id.isEmpty) ConfigFactory.load() else ConfigFactory.load(id)
  implicit val system = ActorSystem("deadman-switch-actor-system", config.resolve())
  implicit val executionContext = system.dispatcher
  implicit val materialize = ActorMaterializer()

  // Init actor API timeout
  val duration = Duration(config.getString("request-timeout"))
  implicit val requestTimeout: Timeout = FiniteDuration(duration.length, duration.unit)

  // Activate replication endpoint
  val location = new NetworkLocation(id)

  // Start REST API server
  implicit val commandApi = new CommandApi(location.commandManager)
  implicit val queryApi = new QueryApi(location.queryManager)
  startup(new HttpRouter().routes)

  // If indicated, start interactive CLI
  if ("cli".equalsIgnoreCase(mode)) {
    system.actorOf(CommandLine.props(location.commandManager, location.queryManager).withDispatcher("dispatchers.cli"))
  }
}

private final class CommandLine(val commandManager: ActorRef, val queryManager: ActorRef) extends Actor with ActorLogging {

  def receive = {

    // Command successes and errors
    case CommandResponse(typ, msg) ⇒
      log.info("Command Response: {}", s"$typ ${msg.mkString(",")}".trim)
      prompt()

    // Query results
    case Tasks(tasks) ⇒
      if (tasks.isEmpty) {
        log.info("No tasks found")
      }
      tasks.foreach { t ⇒
        log.info(t.toString)
      }
      prompt()

    // Process CLI and send commands and/or queries
    case line: String ⇒ line.trim.split(' ').toList match {

      // Commands

      case "schedule" :: key :: agg :: ent :: ttl :: ttw :: tags ⇒
        commandManager ! ScheduleTask(key, agg, ent, Duration(ttl).toMillis, Seq(Duration(ttw).toMillis), tags)

      case "complete" :: key :: agg :: ent :: Nil ⇒
        commandManager ! CompleteTask(key, agg, ent)

      // Query scheduled tasks

      case "query" :: "aggregate" :: id :: Nil ⇒
        queryManager ! GetTasks(AGGREGATE, aggregate = Some(id))

      case "query" :: "entity" :: id :: Nil ⇒
        queryManager ! GetTasks(ENTITY, entity = Some(id))

      case "query" :: "key" :: value :: Nil ⇒
        queryManager ! GetTasks(KEY, key = Some(value))

      // Query expired tasks

      case "query" :: "aggregate" :: "expirations" :: id :: Nil ⇒
        queryManager ! GetExpirations(AGGREGATE, aggregate = Some(id))

      case "query" :: "entity" :: "expirations" :: id :: Nil ⇒
        queryManager ! GetExpirations(ENTITY, entity = Some(id))

      case "query" :: "key" :: "expirations" :: key :: window :: Nil ⇒
        queryManager ! GetByKey(key, window)

      case "query" :: "tag" :: "expirations" :: tag :: window :: Nil ⇒
        queryManager ! GetByTag(tag, window)

      // Query task warnings

      case "query" :: "aggregate" :: "warnings" :: id :: Nil ⇒
        queryManager ! GetWarnings(AGGREGATE, aggregate = Some(id))

      case "query" :: "entity" :: "warnings" :: id :: Nil ⇒
        queryManager ! GetWarnings(ENTITY, entity = Some(id))

      // Catch-all prompt

      case _ ⇒
        prompt()
    }
  }

  private val lines = Source.stdin.getLines

  def prompt(): Unit = if (lines.hasNext) {
    lines.next() match {
      case "exit" ⇒
        context.system.registerOnTermination(System.exit(0))
        val _ = context.system.terminate()
      case line ⇒ self ! line
    }
  }

  override def preStart(): Unit = prompt()
}

object CommandLine {
  def props(commandManager: ActorRef, queryManager: ActorRef): Props = Props(new CommandLine(commandManager, queryManager))
}
