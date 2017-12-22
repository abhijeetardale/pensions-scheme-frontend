/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.Inject

import play.api.Environment
import play.api.libs.json.{Json, Reads}

class CountryOptions @Inject()(environment: Environment) {

  def options(implicit ev: Reads[Map[String, String]]): Seq[InputOption] = {
    val locationStream = environment.resourceAsStream("location-autocomplete-canonical-list.json")

    locationStream.flatMap { in =>
      val locationJsValue = Json.parse(in)

      Json.fromJson[Seq[Seq[String]]](locationJsValue).asOpt.map {
        _.map { countryList =>
          InputOption(countryList(0), countryList(1))
        }
      }
    }.getOrElse(Seq.empty)
  }
}
