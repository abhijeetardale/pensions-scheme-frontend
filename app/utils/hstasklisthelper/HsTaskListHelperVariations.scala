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

package utils.hstasklisthelper

import com.google.inject.Inject
import identifiers.SchemeNameId
import models._
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperVariations @Inject() (allSpokes: AllSpokes) extends HsTaskListHelper(allSpokes) {

  import HsTaskListHelperVariations._

  private[utils] def beforeYouStartSection(userAnswers: UserAnswers, srn: Option[String]): SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(None,
      allSpokes.getBeforeYouStartSpoke(userAnswers, UpdateMode, srn, userAnswers.get(SchemeNameId).getOrElse(""), None),
      Some(Message("messages__schemeTaskList__scheme_information_link_text"))
    )
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers,
                                          mode: Mode, srn: Option[String], viewOnly: Boolean): Option[SchemeDetailsTaskListEntitySection] = {
    (userAnswers.allEstablishersAfterDelete(mode).isEmpty, viewOnly) match {
      case (true, true) =>
        Some(
          SchemeDetailsTaskListEntitySection(None, Nil, None, Message("messages__schemeTaskList__sectionEstablishers_no_establishers"))
        )
      case (true, false) =>
        Some(
          SchemeDetailsTaskListEntitySection(None, Seq(
            EntitySpoke(
              establisherKindLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
                userAnswers.allEstablishers(mode).size, srn, mode), None)), None
          )
        )
      case (false, false) =>
        Some(
          SchemeDetailsTaskListEntitySection(None, Seq(
            EntitySpoke(
              addEstablisherLink(Message("messages__schemeTaskList__sectionEstablishers_view_link"), srn, mode))), None
          )
        )
      case (false, true) => None
    }
  }

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode,
                                      srn: Option[String], viewOnly: Boolean): Option[SchemeDetailsTaskListEntitySection] = {
    (userAnswers.allTrusteesAfterDelete.isEmpty, viewOnly) match {
      case (true, true) =>
        Some(
          SchemeDetailsTaskListEntitySection(None, Nil, None, Message("messages__schemeTaskList__sectionTrustees_no_trustees"))
        )
      case (true, false) =>
        Some(
          SchemeDetailsTaskListEntitySection(None, Seq(
            EntitySpoke(
              trusteeKindLink(Message("messages__schemeTaskList__sectionTrustees_add_link"),
                userAnswers.allTrustees.size, srn, mode), None)
          ), None)
        )
      case (false, false) =>
        Some(
          SchemeDetailsTaskListEntitySection(None, Seq(
            EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_view_link"),
              controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url), None)), None
          )
        )
      case (false, true) =>
        Some(
          SchemeDetailsTaskListEntitySection(None, Nil, None)
        )
    }
  }

  private[utils] def declarationSection(userAnswers: UserAnswers, srn: Option[String], viewOnly: Boolean): Option[SchemeDetailsTaskListEntitySection] = {
    def variationDeclarationLink(userAnswers: UserAnswers, srn: Option[String]): Seq[EntitySpoke] = {
      if (userAnswers.isUserAnswerUpdated) {
        Seq(EntitySpoke(TaskListLink(
          Message("messages__schemeTaskList__declaration_link"),
          if (userAnswers.areVariationChangesCompleted)
            controllers.routes.VariationDeclarationController.onPageLoad(srn).url
          else
            controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url
        ), None))
      } else {
        Nil
      }
    }

    if (viewOnly) {
      None
    } else {
      Some(SchemeDetailsTaskListEntitySection(None,
        variationDeclarationLink(userAnswers, srn),
        Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
        "messages__schemeTaskList__sectionDeclaration_incomplete_v2")
      )
    }
  }

  override def taskList(answers: UserAnswers, viewOnlyOpt: Option[Boolean],
                        srn: Option[String]): SchemeDetailsTaskList = {
    val viewOnly = viewOnlyOpt.getOrElse(false)
    SchemeDetailsTaskList(
      answers.get(SchemeNameId).getOrElse(""),
      srn,
      beforeYouStartSection(answers, srn),
      aboutSection(answers, UpdateMode, srn),
      None,
      addEstablisherHeader(answers, UpdateMode, srn, viewOnly),
      establishersSection(answers, UpdateMode, srn),
      addTrusteeHeader(answers, UpdateMode, srn, viewOnly),
      trusteesSection(answers, UpdateMode, srn),
      declarationSection(answers, srn, viewOnly)
    )
  }
}

object HsTaskListHelperVariations {

  private def trusteeKindLink(linkText: Message, trusteeCount: Int, srn: Option[String], mode: Mode): TaskListLink =
    TaskListLink(linkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, trusteeCount, srn).url)

  private def establisherKindLink(linkText: Message, establisherCount: Int, srn: Option[String], mode: Mode): TaskListLink =
    TaskListLink(linkText, controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, establisherCount, srn).url)

  private def addEstablisherLink(linkText: Message, srn: Option[String], mode: Mode): TaskListLink =
    TaskListLink(linkText, controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url)
}
