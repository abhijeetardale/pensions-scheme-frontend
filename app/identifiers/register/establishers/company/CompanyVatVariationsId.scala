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

package identifiers.register.establishers.company

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.Link
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyVatVariationsId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "companyVat" \ CompanyVatVariationsId.toString
}

object CompanyVatVariationsId {
  override def toString: String = "vat"

  val hiddenLabelVat = "messages__visuallyhidden__establisher__vat_number"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[CompanyVatVariationsId] = {
    new CheckYourAnswers[CompanyVatVariationsId] {

      override def row(id: CompanyVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some("messages__common__cya__vat"), Some(hiddenLabelVat))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(vat) => Seq(AnswerRow("messages__common__cya__vat", Seq(vat), answerIsMessageKey = false, None))
          case _ => Seq(AnswerRow("messages__common__cya__vat", Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", changeUrl, Some(s"${hiddenLabelVat}_add")))))
        }

    }
  }
}




