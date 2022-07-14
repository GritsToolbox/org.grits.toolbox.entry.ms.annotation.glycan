package org.grits.toolbox.entry.ms.annotation.glycan.command;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.glycan.handler.ViewMSGlycanAnnotationResults;

@SuppressWarnings("restriction")
public class ViewMSOverviewCommandExecutor  {
	public static void showMSOverview(IEclipseContext context, Entry entry ) {
		ECommandService commandService = context.get(ECommandService.class);
		EHandlerService handlerService = context.get(EHandlerService.class);
		
		context.set(ViewMSGlycanAnnotationResults.PARAMETER_ID, entry);
		handlerService.executeHandler(
			commandService.createCommand(ViewMSGlycanAnnotationResults.COMMAND_ID, null));
	}
}
