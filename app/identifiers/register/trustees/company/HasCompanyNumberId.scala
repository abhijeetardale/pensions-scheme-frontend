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

package identifiers.register.trustees.company

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class HasCompanyNumberId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = TrusteesId(index).path \ HasCompanyNumberId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) =>
        userAnswers.remove(NoCompanyNumberId(this.index))
      case Some(false) =>
        userAnswers.remove(CompanyRegistrationNumberVariationsId(this.index))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}

object HasCompanyNumberId {
  override def toString: String = "hasCrn"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[HasCompanyNumberId] = {

    def companyName(index: Int) =
      userAnswers.get(CompanyDetailsId(index)) match {
        case Some(companyDetails) => companyDetails.companyName
        case _                    => messages("messages__theCompany")
      }

    def label(index: Int) =
      Some(messages("messages__hasCompanyNumber__h1", companyName(index)))

    def hiddenLabel(index: Int) = Some(messages("messages__visuallyhidden__dynamic_hasCrn", companyName(index)))

    new CheckYourAnswers[HasCompanyNumberId] {
      override def row(id: HasCompanyNumberId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: HasCompanyNumberId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
          case _          => Seq.empty[AnswerRow]
        }
    }
  }

}
