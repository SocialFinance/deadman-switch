package org.sofi.deadman.load

import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.fluent.Request
import org.apache.http.util.EntityUtils
import scala.util.Try

object Http {

  // Task successfully scheduled
  val OK = HttpStatus.SC_OK

  // Captures desired response fields
  final case class HttpResp(status: Int, body: String)

  // Default status code
  private final val serverError = HttpStatus.SC_INTERNAL_SERVER_ERROR

  // Convert apache fluent response to local type
  private def httpResp(response: HttpResponse) = {
    val entity = response.getEntity
    val status = Try(response.getStatusLine.getStatusCode).getOrElse(serverError)
    val body = if (entity == null) "" else EntityUtils.toString(entity)
    HttpResp(status, body)
  }

  // Perform a HTTP POST to the given URL without a body
  def post(url: String) = httpResp(Request.Post(url).execute.returnResponse)

  // Perform a HTTP GET on the given URL
  def get(url: String) = httpResp(Request.Get(url).execute().returnResponse())
}
