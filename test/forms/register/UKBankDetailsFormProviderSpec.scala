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

package forms.register

import forms.behaviours.{BankDetailsBehaviour, FormBehaviours}
import forms.mappings.Constraints
import models._
import models.register.{SortCode, UKBankDetails}
import org.apache.commons.lang3.{RandomStringUtils, RandomUtils}
import org.joda.time.LocalDate

class UKBankDetailsFormProviderSpec extends FormBehaviours with Constraints with BankDetailsBehaviour {

  //scalastyle:off magic.number

  val testSortCode = SortCode("24", "56", "56")
  val testAccountNumber: String = RandomUtils.nextInt(10000000, 99999999).toString

  private val day = LocalDate.now().getDayOfMonth
  private val month = LocalDate.now().getMonthOfYear
  private val year = LocalDate.now().getYear

  val validData: Map[String, String] = Map(
    "bankName" -> "test bank",
    "accountName" -> "test account",
    "sortCode" -> "24 56 56",
    "accountNumber" -> testAccountNumber,
    "date.day" -> s"$day",
    "date.month" -> s"$month",
    "date.year" -> s"${year - 20}"
  )

  val bankDetails = UKBankDetails("test bank", "test account",
    testSortCode, testAccountNumber, new LocalDate(year - 20, month, day))

  val form = new UKBankDetailsFormProvider()()


  "UKBankDetails form" must {
    behave like questionForm(bankDetails)

    behave like formWithMandatoryTextFields(
      Field("bankName", Required -> "messages__error__bank_name"),
      Field("accountName", Required -> "messages__error__account_name"),
      Field("sortCode", Required -> "messages__error__sort_code"),
      Field("accountNumber", Required -> "messages__error__account_number"),
      Field("date.day", Required -> "error.date.day_blank"),
      Field("date.month", Required -> "error.date.month_blank"),
      Field("date.year", Required -> "error.date.year_blank")
    )

    behave like formWithSortCode(
      form,
      "messages__error__sort_code",
      "messages__error__sort_code_invalid",
      "messages__error__sort_code_length",
      Map(
        "bankName" -> "test bank",
        "accountName" -> "test account",
        "accountNumber" -> testAccountNumber,
        "date.day" -> s"$day",
        "date.month" -> s"$month",
        "date.year" -> s"${year - 20}"
      ),
      (bankDetails: UKBankDetails) => bankDetails.sortCode
    )

    "fail to bind when the bank name exceeds max length 28" in {
      val testString = RandomStringUtils.random(29)
      val data = validData + ("bankName" -> testString)

      val expectedError = error("bankName", "messages__error__bank_name_length", 28)
      checkForError(form, data, expectedError)
    }

    "fail to bind when account name exceeds max length 28" in {
      val testString = RandomStringUtils.random(29)
      val data = validData + ("accountName" -> testString)

      val expectedError = error("accountName", "messages__error__account_name_length", 28)
      checkForError(form, data, expectedError)
    }

    Seq("Abffgk", "1 1 1 1 1 1 ", "A1b3j4b2").foreach { code =>
      s"fail to bind when account number $code is invalid" in {
        val data = validData + ("accountNumber" -> code)

        val expectedError = error("accountNumber", "messages__error__account_number_invalid", regexAccountNo)
        checkForError(form, data, expectedError)
      }
    }

    "fail to bind when account number exceeds max length 8" in {
      val data = validData + ("accountNumber" -> "123456789")

      val expectedError = error("accountNumber", "messages__error__account_number_length", 8)
      checkForError(form, data, expectedError)
    }

    "fail to bind when the date is in future" in {
      val tomorrow = LocalDate.now.plusDays(1)
      val data = validData + ("date.day" -> s"${tomorrow.getDayOfMonth}", "date.month" -> s"${tomorrow.getMonthOfYear}", "date.year" -> s"${tomorrow.getYear}")

      val expectedError = error("date", "messages__error__date_future")
      checkForError(form, data, expectedError)
    }
  }
}
