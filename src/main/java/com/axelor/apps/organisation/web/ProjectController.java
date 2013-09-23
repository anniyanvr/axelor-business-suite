/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.service.ProjectService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectController {

	@Inject
	ProjectService projectService;
	
	public void createDefaultTask(ActionRequest request, ActionResponse response) {
		
		Project project = request.getContext().asType(Project.class);
		
		if(project != null && project.getDefaultTask() != null) {			
			projectService.createDefaultTask(project);
			response.setReload(true);
		}
	}
	
	public void createPreSalesTask(ActionRequest request, ActionResponse response) {
		
		Project affair = request.getContext().asType(Project.class);
		
		if(affair != null) {			
			if(projectService.createPreSalesTask(affair) != null)  {
				response.setReload(true);
			}
		}
	}
	
	public void updateFinancialInformation(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Project project = request.getContext().asType(Project.class);
		
		if(project.getId() != null)  {
			projectService.updateFinancialInformation(project);
			
			response.setValue("initialTurnover", project.getInitialTurnover());
			response.setValue("initialCost", project.getInitialCost());
			response.setValue("initialMargin", project.getInitialMargin());
			response.setValue("estimatedTurnover", project.getEstimatedTurnover());
			response.setValue("estimatedCost", project.getEstimatedCost());
			response.setValue("estimatedMargin", project.getEstimatedMargin());
			response.setValue("realizedTurnover", project.getRealizedTurnover());
			response.setValue("realizedCost", project.getRealizedCost());
			response.setValue("realizedMargin", project.getRealizedMargin());
		}
	}
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printProjectReport(ActionRequest request, ActionResponse response) {

		Project project = request.getContext().asType(Project.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();

		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Project.rptdesign&__format="+project.getExportTypeSelect()+"&ProjectId="+project.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));


		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){


			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Name "+project.getAffairName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
