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

package identifiers.register.establishers.company.director

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId}
import models.AddressYears
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import play.api.mvc.AnyContent
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class DirectorAddressYearsId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[AddressYears] {

  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.remove(DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex))
          .flatMap(_.remove(DirectorPreviousAddressId(establisherIndex, directorIndex)))
          .flatMap(_.remove(DirectorPreviousAddressListId(establisherIndex, directorIndex)))
      case Some(AddressYears.UnderAYear) =>
        userAnswers.set(IsDirectorCompleteId(establisherIndex, directorIndex))(false).flatMap(
          _.set(IsEstablisherCompleteId(establisherIndex))(false)
        )
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }

}

object DirectorAddressYearsId {
  override lazy val toString: String = "companyDirectorAddressYears"

  implicit def cya(implicit messages: Messages, userAnswers: UserAnswers, rq: DataRequest[AnyContent]): CheckYourAnswers[DirectorAddressYearsId] = {
    def cyaText(id: DirectorAddressYearsId): (String, String) = {
      userAnswers.get(DirectorDetailsId(id.establisherIndex, id.directorIndex)) match {
        case Some(director) =>
          (messages("messages__director_address_years__cya_withName", director.fullName),
          messages("messages__visuallyhidden__director__address_years_withName", director.fullName))
        case _ =>
          (messages("messages__director_address_years__cya"),
          messages("messages__visuallyhidden__director__address_years"))
      }
    }

    new CheckYourAnswers[DirectorAddressYearsId] {
      override def row(id: DirectorAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressYearsCYA(cyaText(id)._1, cyaText(id)._2)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) =>
            AddressYearsCYA(cyaText(id)._1, cyaText(id)._2)().row(id)(changeUrl, userAnswers)
          case _ =>
            AddressYearsCYA(cyaText(id)._1, cyaText(id)._2)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
