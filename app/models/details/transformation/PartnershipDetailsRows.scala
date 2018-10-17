/*
 * Copyright 2018 HM Revenue & Customs
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

package models.details.transformation

import javax.inject.Inject
import models.details._
import utils.CountryOptions
import viewmodels.AnswerRow

import scala.language.implicitConversions

//noinspection SpellCheckingInspection
//scalastyle:off method.length
case class PartnershipDetailsRows[I <: PartnershipDetails] @Inject()(countryOptions: CountryOptions) extends TransformedElement[I] {

  override val entityType: String = "partnership"

  override def transformRows(data: I): Seq[AnswerRow] = {

    vatRegistrationRows(data.vatRegistration) ++
      payeRefRows(data.payeRef) ++
      utrRows(data.utr) ++
      addressRows(countryOptions, data.address) ++
      previousAddressRows(countryOptions, Some(data.previousAddress)) ++
      contactRows(data.contact)
  }


}
