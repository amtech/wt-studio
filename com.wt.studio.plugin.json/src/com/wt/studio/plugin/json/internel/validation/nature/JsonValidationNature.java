/**
 *
 */
package com.wt.studio.plugin.json.internel.validation.nature;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.wt.studio.plugin.json.internel.log.JsonLog;
import com.wt.studio.plugin.json.internel.validation.IncrementalJsonValidator;

/**
 * Project nature allows the request project to validate any com.boothen.jsonedit.core files.
 *
 * @author Matt Garner
 *
 */
public class JsonValidationNature implements IProjectNature {

	public static final String NATURE_ID = "com.boothen.jsonedit.validation.nature";

	private IProject project;

	@Override
	public void configure() throws CoreException {
		IncrementalJsonValidator.addBuilderToProject(project);
		new Job("Validating JSON Files") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					project.build(IncrementalJsonValidator.FULL_BUILD, IncrementalJsonValidator.BUILDER_ID, null,monitor);

				} catch (CoreException e) {
					JsonLog.logError(e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void deconfigure() throws CoreException {
		IncrementalJsonValidator.removeBuilderFromProject(project);
		IncrementalJsonValidator.deleteValidationMarkers(project);
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	public static void addNature(IProject project) {

		if (!project.isOpen()) {
			return;
		}

		// Get the description
		IProjectDescription description;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			JsonLog.logError("Error get project description: ", e);
			return;
		}

		List<String> newIds = new ArrayList<String>();
		String[] natureIds = description.getNatureIds();
		for(String natureId : natureIds) {
			if (natureId.equals(NATURE_ID)) {
				return;
			}
		}

		newIds.addAll(Arrays.asList(natureIds));
		newIds.add(NATURE_ID);

		// Save description
		description.setNatureIds(newIds.toArray(new String[newIds.size()]));
		try {
			project.setDescription(description, null);
		} catch (CoreException e) {
			JsonLog.logError("Error set project description: ", e);
		}

	}

	public static boolean hasNature(IProject project) {

		try {
			return project.isOpen() && project.hasNature(NATURE_ID);
		} catch (CoreException e) {
			JsonLog.logError("Error determining if project has nature.: ", e);
			return false;
		}
	}

	public static void removeNature(IProject project) {

		if (!project.isOpen()) {
			return;
		}

		// Get the description
		IProjectDescription description;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			JsonLog.logError("Error get project description: ", e);
			return;
		}

		List<String> newIds = new ArrayList<String>();
		String[] natureIds = description.getNatureIds();
		newIds.addAll(Arrays.asList(natureIds));
		if (newIds.indexOf(NATURE_ID) == -1) {
			return;
		}

		newIds.remove(NATURE_ID);

		// Save description
		description.setNatureIds(newIds.toArray(new String[newIds.size()]));
		try {
			project.setDescription(description, null);
		} catch (CoreException e) {
			JsonLog.logError("Error set project description: ", e);
		}
	}
}
