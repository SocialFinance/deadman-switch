package org.sofi.deadman

import akka.actor._
import akka.util.Timeout
import org.sofi.deadman.http._
import org.sofi.deadman.location.NetworkLocation
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.io.Source
import scala.util.Try

object Main extends App with Server {

  // Read args
  val id = Try(args(0)).getOrElse("loc1")
  val mode = Try(args(1)).getOrElse("")

  // Boot actor system
  implicit val system = ActorSystem("deadman-switch-actor-system", ConfigFactory.load(id).resolve())
  implicit val executionContext = system.dispatcher

  // Init actor API timeout
  val duration = Duration(system.settings.config.getString("request-timeout"))
  implicit val requestTimeout: Timeout = FiniteDuration(duration.length, duration.unit)

  // Activate replication endpoint
  val location = new NetworkLocation(id)

  // Start REST API server
  implicit val api = new ApiFunctions(location.commandManager, location.queryManager)
  startup(new HttpRouter().routes)

  // If indicated, start interactive CLI
  if ("cli".equalsIgnoreCase(mode)) {
    system.actorOf(CommandLine.props(location.commandManager, location.queryManager).withDispatcher("dispatchers.cli"))
  }
}

private final class CommandLine(val commandManager: ActorRef, val queryManager: ActorRef)
  extends Actor with ActorLogging {

  def receive = {

    // Command successes and errors
    case CommandResponse(msg, typ) ⇒
      log.info(s"Command Response: $typ $msg".trim)
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

    case Count(count) ⇒
      log.info(s"Found $count tasks")
      prompt()

    // Process CLI and send commands and/or queries
    case line: String ⇒ line.trim.split(' ').toList match {

      // Commands

      case "schedule" :: key :: agg :: ent :: ttl :: ttw :: tags ⇒
        commandManager ! ScheduleTask(key, agg, ent, ttl.toLong, Seq(ttw.toLong), tags)

      case "complete" :: key :: agg :: ent :: Nil ⇒
        commandManager ! CompleteTask(key, agg, ent)

      // Query scheduled tasks

      case "query" :: "scheduled" :: "aggregate" :: id :: Nil ⇒
        queryManager ! GetTasks(QueryType.AGGREGATE, aggregate = Some(id))

      case "query" :: "scheduled" :: "entity" :: id :: Nil ⇒
        queryManager ! GetTasks(QueryType.ENTITY, entity = Some(id))

      case "query" :: "scheduled" :: "key" :: value :: Nil ⇒
        queryManager ! GetTasks(QueryType.KEY, key = Some(value))

      // Query expired tasks

      case "query" :: "expired" :: "aggregate" :: id :: Nil ⇒
        queryManager ! GetExpirations(QueryType.AGGREGATE, aggregate = Some(id))

      case "query" :: "expired" :: "entity" :: id :: Nil ⇒
        queryManager ! GetExpirations(QueryType.ENTITY, entity = Some(id))

      case "query" :: "expired" :: "tag" :: tag :: window :: Nil ⇒
        queryManager ! GetTags(tag, window)

      // Query task warnings

      case "query" :: "warnings" :: "aggregate" :: id :: Nil ⇒
        queryManager ! GetWarnings(QueryType.AGGREGATE, aggregate = Some(id))

      case "query" :: "warnings" :: "entity" :: id :: Nil ⇒
        queryManager ! GetWarnings(QueryType.ENTITY, entity = Some(id))

      // Query for task counts

      case "count" :: "aggregate" :: id :: Nil ⇒
        queryManager ! GetCount(QueryType.AGGREGATE, aggregate = Some(id))

      case "count" :: "entity" :: id :: Nil ⇒
        queryManager ! GetCount(QueryType.ENTITY, entity = Some(id))

      case "count" :: "key" :: value :: Nil ⇒
        queryManager ! GetCount(QueryType.KEY, key = Some(value))

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
  def props(commandManager: ActorRef, queryManager: ActorRef): Props =
    Props(new CommandLine(commandManager, queryManager))
}
