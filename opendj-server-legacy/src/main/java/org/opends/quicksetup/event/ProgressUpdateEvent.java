/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2006-2008 Sun Microsystems, Inc.
 * Portions Copyright 2014 ForgeRock AS.
 */

package org.opends.quicksetup.event;

import org.forgerock.i18n.LocalizableMessage;
import org.opends.quicksetup.ProgressStep;

/**
 * The event that is generated when there is a change during the installation
 * process (we get a new log message when starting the server, or we finished
 * configuring the server for instance).
 *
 * In the current implementation this events are generated by the Installer
 * objects and are notified to the objects implementing
 * ProgressUpdateListener (QuickSetup object).
 *
 */
public class ProgressUpdateEvent {
  private ProgressStep step;

  private Integer progressRatio;

  private LocalizableMessage currentPhaseSummary;

  private LocalizableMessage newLogs;

  /**
   * Constructor of the ProgressUpdateEvent.
   * @param step the ProgressStep object describing in which step
   * of the installation we are (configuring server, starting server, etc.)
   * @param progressRatio the integer that specifies which percentage of
 * the whole installation has been completed.
   * @param currentPhaseSummary the localized summary message for the
* current installation progress.
   * @param newLogs the new log messages that we have for the installation.
   */
  public ProgressUpdateEvent(ProgressStep step,
      Integer progressRatio, LocalizableMessage currentPhaseSummary, LocalizableMessage newLogs)
  {
    this.step = step;
    this.progressRatio = progressRatio;
    this.currentPhaseSummary = currentPhaseSummary;
    this.newLogs = newLogs;
  }

  /**
   * Gets a localized message summary describing the install progress
   * status.
   * @return the localized message summary describing the progress status.
   */
  public LocalizableMessage getCurrentPhaseSummary()
  {
    return currentPhaseSummary;
  }

  /**
   * Gets the new logs for the install progress.
   * @return the new logs for the current install progress.
   */
  public LocalizableMessage getNewLogs()
  {
    return newLogs;
  }

  /**
   * Gets the progress ratio for the install progress.
   * @return the progress ratio for the install progress.
   */
  public Integer getProgressRatio()
  {
    return progressRatio;
  }

  /**
   * Gets the current progress step.
   * @return the current progress step.
   */
  public ProgressStep getProgressStep()
  {
    return step;
  }
}
