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

package utils

import identifiers.register.trustees.MoreThanTenTrusteesId
import identifiers.{IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId, _}
import models.register.Entity
import models.{Link, UpdateMode}
import play.api.i18n.Messages
import viewmodels._

class HsTaskListHelperVariations(answers: UserAnswers, viewOnly: Boolean, srn: Option[String])(implicit messages: Messages) extends HsTaskListHelper(answers) {

  override protected lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__scheme_info_link_text")

  override protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.get(IsAboutMembersCompleteId) match {
      case Some(true) => Link(aboutMembersLinkText, controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url)
      case _ => Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
    }

    val benefitsAndInsuranceLink = userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId) match {
      case Some(true) => Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url)
      case _ => Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url)
    }

    Seq(SchemeDetailsTaskListSection(userAnswers.get(IsAboutMembersCompleteId), membersLink, None),
      SchemeDetailsTaskListSection(userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId), benefitsAndInsuranceLink, None))
  }

  protected[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection] =
    if (viewOnly) {
      None
    } else {
      Some(SchemeDetailsTaskListDeclarationSection(declarationLink(userAnswers)))
    }

  private def listOfSectionNameAsLink(sections: Seq[Entity[_]]): Seq[SchemeDetailsTaskListSection] = {
    val notDeletedElements = for ((section, index) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        Some(SchemeDetailsTaskListSection(
          Some(section.isCompleted),
          Link(messages("messages__schemeTaskList__persons_details__link_text", section.name),
            linkTarget(section, index, UpdateMode, srn)),
          None)
        )
      }
    }
    notDeletedElements.flatten
  }

  protected[utils] def establishers(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] =
    listOfSectionNameAsLink(userAnswers.allEstablishers)

  protected[utils] def trustees(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] =
    listOfSectionNameAsLink(userAnswers.allTrustees)

  override def declarationEnabled(userAnswers: UserAnswers): Boolean = {
    val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
    Seq(
      userAnswers.get(IsBeforeYouStartCompleteId),
      userAnswers.get(IsAboutMembersCompleteId),
      userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
      Some(isAllEstablishersCompleted(userAnswers)),
      Some(isTrusteeOptional | isAllTrusteesCompleted(userAnswers)),
      Some(userAnswers.allTrusteesAfterDelete.size < 10 || userAnswers.get(MoreThanTenTrusteesId).isDefined)
    ).forall(_.contains(true)) && userAnswers.isUserAnswerUpdated()
  }

  def taskList: SchemeDetailsTaskList = {
    val schemeName = answers.get(SchemeNameId).getOrElse("")
    SchemeDetailsTaskList(
      beforeYouStartSection(answers, UpdateMode, srn),
      messages("messages__schemeTaskList__about_scheme_header", schemeName),
      aboutSection(answers),
      None,
      addEstablisherHeader(answers, UpdateMode, srn),
      establishers(answers),
      addTrusteeHeader(answers, UpdateMode, srn),
      trustees(answers),
      declarationSection(answers),
      answers.get(SchemeNameId).getOrElse(""),
      messages("messages__scheme_details__title"),
      Some(messages("messages__schemeTaskList__scheme_information_link_text")),
      messages("messages__scheme_details__title")
    )

  }
}