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

package utils

import connectors.FakeUserAnswersCacheConnector
import identifiers.TypedIdentifier
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

object FakeSectionComplete extends SectionComplete with FakeUserAnswersCacheConnector {

  override def setCompleteFlag(cacheId: String, id: TypedIdentifier[Boolean], userAnswers: UserAnswers, value: Boolean = true)
                              (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] = {
    save("cacheId", id, value) map UserAnswers
  }
}