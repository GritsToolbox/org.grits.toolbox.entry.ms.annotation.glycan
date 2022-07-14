package org.grits.toolbox.entry.ms.annotation.glycan.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPrecursorPeak;
import org.grits.toolbox.entry.ms.annotation.glycan.Activator;
import org.grits.toolbox.entry.ms.extquantfiles.process.ExtractDataProcessor;
import org.grits.toolbox.ms.om.data.IntensityFilter;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;

public class FileUtils {
	
	private static final Logger logger = Logger.getLogger(FileUtils.class);
	
	private static final String configFolderName = "org.grits.toolbox.entry.ms";
	private static final String filterHelpFolderName = "files";
	private static final String FILTER_FILE = "filters-ms.xml";
	public static final URL LIB_URL = Platform.getBundle(Activator.PLUGIN_ID).getResource("preference");
	public static final URL FILES_URL = Platform.getBundle(Activator.PLUGIN_ID).getResource("files");

	private static final String FILTER_HELP_FILE = "filterHelpMainPage.htm";
	
	@Inject @Named (IGritsConstants.WORKSPACE_LOCATION)
	private static String workspaceLocation;
	@Inject @Named (IGritsConstants.CONFIG_LOCATION)
	private static String configLocation;
	
	public static void copyFilterFile() throws IOException {
		String configFolderLocation = configLocation + File.separator + configFolderName;
    	File configFolder = new File (configFolderLocation);
    	if (!configFolder.exists())
    		configFolder.mkdirs();
    	
    	File filterFile = new File(configFolderLocation + File.separator + FILTER_FILE);
    	if (!filterFile.exists()) {
    		// check for the library in the jar file
    		// copy from jar if exists	
    		URL resourceFileUrl = FileLocator.toFileURL(LIB_URL);
            String originalJarFilePath = resourceFileUrl.getPath() + FILTER_FILE;
            File originalJarFile = new File(originalJarFilePath);
            FileOutputStream configFile = new FileOutputStream(configFolderLocation + File.separator + FILTER_FILE);
            Files.copy(originalJarFile.toPath(), configFile);
            configFile.close();
    	}
    	else {
    		// check for versions, if the jar file contains a newer version, replace the one in configuration directory
    		//List<Class> contextList = new ArrayList<Class>(Arrays.asList(FilterUtils.filterClassContext));
    		List<Class> contextList = new ArrayList<Class>(Arrays.asList(AnnotationReader.filterClassContext));
			contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
		    contextList.add(FiltersLibrary.class);
    		JAXBContext context;
			try {
				context = JAXBContext.newInstance(contextList.toArray(new Class[contextList.size()]));
				URL resourceFileUrl = FileLocator.toFileURL(LIB_URL);
	            String originalJarFilePath = resourceFileUrl.getPath() + FILTER_FILE;
				FiltersLibrary fromJar = FilterUtils.readCustomFilters (originalJarFilePath, context);
				FiltersLibrary existing = FilterUtils.readCustomFilters(filterFile.getAbsolutePath(), context);
				if (FilterUtils.needToCopyFilterLibraryFromJar(fromJar, existing)) {
				   // replace it with the one from the jar but first check if merge is necessary
					if (!FilterUtils.mergeFilterLibraries(fromJar, existing)) { // no merge, we can overwrite
						File originalJarFile = new File(originalJarFilePath);
			            File configFile = new File(configFolderLocation + File.separator + FILTER_FILE);
			            Files.copy(originalJarFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} else { // merged
						// save the existing to reflect changes
						FilterUtils.updateLibrary(existing, filterFile.getAbsolutePath(), context);
					}
				}
				
			} catch (JAXBException e) {
				// ignore the error
				// copy from the jar
				logger.warn ("Could not load the filter xml file from the configuration folder, copying anyway from the jar");
				URL resourceFileUrl = FileLocator.toFileURL(LIB_URL);
	            String originalJarFilePath = resourceFileUrl.getPath() + FILTER_FILE;
	            File originalJarFile = new File(originalJarFilePath);
	            File configFile = new File(configFolderLocation + File.separator + FILTER_FILE);
	            Files.copy(originalJarFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				logger.warn ("Merging of the filters failed! Copying anyway from the jar", e);
				URL resourceFileUrl = FileLocator.toFileURL(LIB_URL);
	            String originalJarFilePath = resourceFileUrl.getPath() + FILTER_FILE;
	            File originalJarFile = new File(originalJarFilePath);
	            File configFile = new File(configFolderLocation + File.separator + FILTER_FILE);
	            Files.copy(originalJarFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
    		
    	}
	}
	
	/** 
	 * copy filter help html files and images into the configuration directory
	 * from "files" folder in the jar
	 */
	public static void copyFilterHelpFiles() {
		String configFolderLocation = configLocation + File.separator + configFolderName;
    	File configFolder = new File (configFolderLocation);
    	if (!configFolder.exists())
    		configFolder.mkdirs();
    	
    	File configFilesFolder = new File (configFolderLocation + File.separator + filterHelpFolderName);
    	if (!configFilesFolder.exists())
    		configFilesFolder.mkdirs();
    	
		// check for the help files in the jar file
		// copy from jar if exists	
    	URL resourceFileUrl;
		try {
			resourceFileUrl = FileLocator.toFileURL(FILES_URL);
	        File originalJarFile = new File(resourceFileUrl.getPath());
	        copyFolder (originalJarFile, configFilesFolder);
		} catch (Exception e) {
			logger.error("Could not copy filter help files", e);
		}
	}
	
	/**
	 * copies a source folder (or a file) to destination folder (or file)
	 * @param source source file 
	 * @param destination destination file
	 * @throws IOException if files do not exist or cannot be copied for some reason
	 */
	public static void copyFolder(File source, File destination) throws IOException {
	    if (source.isDirectory()) {
	        if (!destination.exists()) {
	            destination.mkdirs();
	        }

	        String files[] = source.list();
	        for (String file : files) {
	            File srcFile = new File(source, file);
	            File destFile = new File(destination, file);

	            copyFolder(srcFile, destFile);
	        }
	    } else {
	    	FileOutputStream configFile = new FileOutputStream(destination);
	        Files.copy(source.toPath(), configFile);
	        configFile.close();
	    }
	}
	
	public static String getFilterPath () {
		String configFolderLocation = configLocation + File.separator + configFolderName;
		return configFolderLocation + File.separator + FILTER_FILE;
	}
	
	public static String getFilterHelpPath() {
		String configFolderLocation = configLocation + File.separator + configFolderName;
		return configFolderLocation + File.separator + filterHelpFolderName + File.separator + FILTER_HELP_FILE;
	}
	
	public static FiltersLibrary readFilters (String filePath) throws UnsupportedEncodingException, JAXBException, FileNotFoundException {
		FiltersLibrary library = new FiltersLibrary();
		try {
			List<Filter> currentFilters = new ArrayList<>();
			List<Class> contextList = new ArrayList<Class>(Arrays.asList(AnnotationReader.filterClassContext));
			contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
		    contextList.add(FiltersLibrary.class);
			JAXBContext context = JAXBContext.newInstance(contextList.toArray(new Class[contextList.size()]));
			FiltersLibrary newFilterSet = FilterUtils.readCustomFilters(filePath, context);
			if (newFilterSet != null) {
				currentFilters.addAll(newFilterSet.getFilters());
			}
			
			library.setFilters(currentFilters);
			library.setCategories(newFilterSet.getCategories());
		} catch (UnsupportedEncodingException | FileNotFoundException | JAXBException e1) {
			throw e1;
		}
		
		// add custom filters (eg. intensity filter)
		// precursor intensity
		IntensityFilter precursorIntensityFilter = new IntensityFilter();
		precursorIntensityFilter.setDescription("Give a range for precursor intensity");
		precursorIntensityFilter.setLabel("Precursor Intensity");
		precursorIntensityFilter.setName("Precursor Intensity");
		precursorIntensityFilter.setIntensityColumnKey(DMPrecursorPeak.precursor_peak_intensity.name());
		
		library.getFilters().add(precursorIntensityFilter);
		
		// peak intensity
		IntensityFilter peakIntensityFilter = new IntensityFilter();
		peakIntensityFilter.setDescription("Give a range for peak intensity");
		peakIntensityFilter.setLabel("Peak Intensity");
		peakIntensityFilter.setName("Peak Intensity");
		precursorIntensityFilter.setIntensityColumnKey(DMPeak.peak_intensity.name());
		
		library.getFilters().add(peakIntensityFilter);
		
		// extract intensity
		IntensityFilter extractIntensityFilter = new IntensityFilter();
		extractIntensityFilter.setDescription("Give a range for extract total intensity");
		extractIntensityFilter.setLabel("Extract Intensity");
		extractIntensityFilter.setName("Extract Intensity");
		// this column name is extracted from org.grits.toolbox.entry.ms.extquantfiles.process.ExternalQuantFileProcessor
		// if that naming scheme is changed, this needs to be changed!!!
//		extractIntensityFilter.setIntensityColumnKey(ExtractDataProcessor.DEFAULT_KEY + "_quant_total_intensity");    
		extractIntensityFilter.setIntensityColumnKey("_quant_total_intensity");    
		
		library.getFilters().add(extractIntensityFilter);
		
		return library;
	}

	/**
	 * need to update the user's existing filter library
	 * consider the version numbers and merge files if necessary
     *
	 * @param filterPath
	 * @param filterLibrary
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void updateLibrary(String filterPath, FiltersLibrary filterLibrary) throws JAXBException, IOException {
		List<Class> contextList = new ArrayList<Class>(Arrays.asList(AnnotationReader.filterClassContext));
		contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
	    contextList.add(FiltersLibrary.class);
		JAXBContext context = JAXBContext.newInstance(contextList.toArray(new Class[contextList.size()]));
		FilterUtils.updateLibrary (filterLibrary, filterPath, context);
	}

}