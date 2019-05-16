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

package controllers.actions


import com.google.inject.{ImplementedBy, Inject}
import connectors._
import identifiers.SchemeStatusId
import models._
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import play.api.libs.json.JsValue
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalImpl(dataConnector: UserAnswersCacheConnector,
                        viewConnector: SchemeDetailsReadOnlyCacheConnector,
                        updateConnector: UpdateSchemeCacheConnector,
                        lockConnector: PensionSchemeVarianceLockConnector,
                        mode: Mode,
                        srn: Option[String]) extends DataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    mode match {
      case NormalMode | CheckMode => getOptionalRequest(dataConnector.fetch(request.externalId), viewOnly = false)(request)

      case UpdateMode | CheckUpdateMode =>
        srn.map { srn =>
          lockConnector.isLockByPsaIdOrSchemeId(request.psaId.id, srn).flatMap {
            case Some(VarianceLock) =>
              getOptionalRequest(updateConnector.fetch(srn), viewOnly = false)(request)
            case Some(_) =>
              getOptionalRequest(viewConnector.fetch(request.externalId), viewOnly = true)(request)
            case None =>
              getRequestWithNoLock(request)
          }
        }.getOrElse(Future(OptionalDataRequest(request.request, request.externalId, None, request.psaId, false)))
    }
  }

  private def getRequestWithNoLock[A](request: AuthenticatedRequest[A])(implicit hc: HeaderCarrier): Future[OptionalDataRequest[A]] = {
    viewConnector.fetch(request.externalId).map {
      case Some(answersJsValue) =>
        UserAnswers(answersJsValue).get(SchemeStatusId) match {
          case Some(x) if x == "Open" =>
            OptionalDataRequest(request.request, request.externalId, Some(UserAnswers(answersJsValue)), request.psaId, false)
          case _ =>
            OptionalDataRequest(request.request, request.externalId, Some(UserAnswers(answersJsValue)), request.psaId, true)
        }
      case None =>
        OptionalDataRequest(request.request, request.externalId, None, request.psaId, false)
    }
  }

  def getOptionalRequest[A](f: Future[Option[JsValue]], viewOnly: Boolean)(implicit request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] =
    f.map {
      case None => OptionalDataRequest(request.request, request.externalId, None, request.psaId, viewOnly)
      case Some(data) => OptionalDataRequest(request.request, request.externalId, Some(UserAnswers(data)), request.psaId, viewOnly)
    }
}

@ImplementedBy(classOf[DataRetrievalImpl])
trait DataRetrieval extends ActionTransformer[AuthenticatedRequest, OptionalDataRequest]

class DataRetrievalActionImpl @Inject()(dataConnector: UserAnswersCacheConnector,
                                        viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                        updateConnector: UpdateSchemeCacheConnector,
                                        lockConnector: PensionSchemeVarianceLockConnector
                                       ) extends DataRetrievalAction {
  override def apply(mode: Mode, srn: Option[String]): DataRetrieval =
    new DataRetrievalImpl(dataConnector, viewConnector, updateConnector, lockConnector, mode, srn)
}

@ImplementedBy(classOf[DataRetrievalActionImpl])
trait DataRetrievalAction {
  def apply(mode: Mode = NormalMode, srn: Option[String] = None): DataRetrieval
}
