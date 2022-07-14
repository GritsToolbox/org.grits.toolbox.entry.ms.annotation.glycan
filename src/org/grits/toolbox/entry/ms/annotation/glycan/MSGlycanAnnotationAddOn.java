package org.grits.toolbox.entry.ms.annotation.glycan;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;

public class MSGlycanAnnotationAddOn {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationAddOn.class);

	@Inject
	@Optional
	public void applicationStarted(
			IEclipseContext eclipseContext) {
		eclipseContext.set(FileUtils.class,
				ContextInjectionFactory.make(FileUtils.class, eclipseContext));
		try {
			FileUtils.copyFilterFile();
			FileUtils.copyFilterHelpFiles();
		} catch (IOException e) {
			logger.error("Filter file cannot be copied. Reason: ", e);
		}
	}

}
