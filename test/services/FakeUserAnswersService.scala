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

package services

import config.FrontendAppConfig
import connectors.{FakeFrontendAppConfig, FakeLockConnector, FakeSubscriptionCacheConnector, FakeUpdateCacheConnector, PensionSchemeVarianceLockConnector, SubscriptionCacheConnector, UpdateSchemeCacheConnector}
import identifiers.TypedIdentifier
import models.Mode
import models.requests.DataRequest
import org.scalatest.Matchers
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait FakeUserAnswersService extends UserAnswersService with Matchers {

  override protected def subscriptionCacheConnector: SubscriptionCacheConnector = FakeSubscriptionCacheConnector.getConnector
  override protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector = FakeUpdateCacheConnector.getConnector
  override protected def lockConnector: PensionSchemeVarianceLockConnector = FakeLockConnector.getConnector
    override val appConfig: FrontendAppConfig =  FakeFrontendAppConfig.getConfig

  private val data: mutable.HashMap[String, JsValue] = mutable.HashMap()
  private val removed: mutable.ListBuffer[String] = mutable.ListBuffer()

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
  {
    data += (id.toString -> Json.toJson(value))
    Future.successful(Json.obj())
  }

  override def setCompleteFlag(mode: Mode, srn: Option[String], id: TypedIdentifier[Boolean], userAnswers: UserAnswers, value: Boolean)
                                               (implicit fmt: Format[Boolean], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[UserAnswers] =
  {
    data += (id.toString -> Json.toJson(value))
    Future.successful(UserAnswers())
  }

  override def upsert(mode: Mode, srn: Option[String], value: JsValue)
            (implicit ec: ExecutionContext, hc: HeaderCarrier,
             request: DataRequest[AnyContent]): Future[JsValue] = {
    data += ("userAnswer" -> Json.toJson(value))
    Future.successful(value)
  }

  override def remove[I <: TypedIdentifier[_]](mode: Mode, srn: Option[String], id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] = {
    removed += id.toString
    Future.successful(Json.obj())
  }

  def fetch(cacheId: String)(implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier
  ): Future[Option[JsValue]] = {

    Future.successful(Some(Json.obj()))
  }

  def lastUpdated(cacheId: String)(implicit
                                            ec: ExecutionContext,
                                            hc: HeaderCarrier
  ): Future[Option[JsValue]] = {

    Future.successful(Some(Json.obj()))
  }

  def userAnswer: UserAnswers = {
    UserAnswers(data.get("userAnswer").getOrElse(Json.obj()))
  }

  def verify[A, I <: TypedIdentifier[A]](id: I, value: A)(implicit fmt: Format[A]): Unit = {
    data should contain(id.toString -> Json.toJson(value))
  }

  def verifyNot(id: TypedIdentifier[_]): Unit = {
    data should not contain key(id.toString)
  }

  def verifyRemoved(id: TypedIdentifier[_]): Unit = {
    removed should contain(id.toString)
  }

  def removeAll(cacheId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    Future.successful(Ok)
  }

  def reset(): Unit = {
    data.clear()
    removed.clear()
  }
}

object FakeUserAnswersService extends FakeUserAnswersService


