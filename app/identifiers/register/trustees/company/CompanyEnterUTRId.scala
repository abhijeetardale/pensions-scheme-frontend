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

package identifiers.register.trustees.company

import identifiers.TypedIdentifier
import identifiers.register.trustees
import identifiers.register.trustees.TrusteesId
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyEnterUTRId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ CompanyEnterUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(CompanyNoUTRReasonId(this.index))
}

object CompanyEnterUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[CompanyEnterUTRId] = {

    def companyName(index: Int) =
      userAnswers.get(CompanyDetailsId(index)) match {
        case Some(companyDetails) => companyDetails.companyName
        case _                    => messages("messages__theCompany")
      }

    val label: String = "messages__utr__checkyouranswerslabel"
    def hiddenLabel(index: Int) = messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference", companyName(index))

    new CheckYourAnswers[CompanyEnterUTRId] {
      override def row(id: CompanyEnterUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[CompanyEnterUTRId](label, hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyEnterUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(trustees.IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[CompanyEnterUTRId](label, hiddenLabel(id.index))().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }

}

