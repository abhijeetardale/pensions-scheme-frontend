/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import config.FrontendAppConfig
import identifiers.TypedIdentifier
import models.requests.DataRequest
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http._
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

trait CacheConnector extends UserAnswersCacheConnector{

  val config: FrontendAppConfig
  val http: WSClient
  protected def url(id: String): String

  protected def lastUpdatedUrl(id: String): String

  override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)
                                               (implicit
                                                fmt: Format[A],
                                                ec: ExecutionContext,
                                                hc: HeaderCarrier
                                               ): Future[JsValue] = {
    modify(cacheId, _.set(id)(value))
  }

  override  def upsert(cacheId: String, value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    modify(cacheId, _ => JsSuccess(UserAnswers(value)))

  override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier
                                     ): Future[JsValue] = {
    modify(cacheId, _.remove(id))
  }

  private[connectors] def modify(cacheId: String, modification: UserAnswers => JsResult[UserAnswers])
                                (implicit
                                 ec: ExecutionContext,
                                 hc: HeaderCarrier
                                ): Future[JsValue] = {
    fetch(cacheId).flatMap {
      json =>
        modification(UserAnswers(json.getOrElse(Json.obj()))) match {
          case JsSuccess(UserAnswers(updatedJson), _) =>
            http.url(url(cacheId))
              .withHeaders(hc.withExtraHeaders(("content-type", "application/json")).headers: _*)
              .post(PlainText(Json.stringify(updatedJson)).value).flatMap {
              response =>
                response.status match {
                  case OK =>
                    Future.successful(updatedJson)
                  case _ =>
                    Future.failed(new HttpException(response.body, response.status))
                }
            }
          case JsError(errors) =>
            Future.failed(JsResultException(errors))
        }
    }
  }

  override def fetch(id: String)(implicit
                                 ec: ExecutionContext,
                                 hc: HeaderCarrier
  ): Future[Option[JsValue]] = {

    http.url(url(id))
      .withHeaders(hc.headers: _*)
      .get()
      .flatMap {
        response =>
          response.status match {
            case NOT_FOUND =>
              Future.successful(None)
            case OK =>
              Future.successful(Some(Json.parse(response.body)))
            case _ =>
              Future.failed(new HttpException(response.body, response.status))
          }
      }
  }

  override def removeAll(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    http.url(url(id))
      .withHeaders(hc.headers: _*)
      .delete().map(_ => Ok)
  }

  override def lastUpdated(id: String)(implicit
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier
  ): Future[Option[JsValue]] = {

    http.url(lastUpdatedUrl(id))
      .withHeaders(hc.headers: _*)
      .get().flatMap {
      response =>
        response.status match {
          case NOT_FOUND =>
            Future.successful(None)
          case OK =>
            Logger.debug(s"connectors.MicroserviceCacheConnector.fetch: Successful response: ${response.body}")
            Future.successful(Some(Json.parse(response.body)))
          case _ =>
            Future.failed(new HttpException(response.body, response.status))
        }
    }
  }
}
