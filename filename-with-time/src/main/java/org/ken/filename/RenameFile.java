package org.ken.filename;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class RenameFile {
	
	private static final Set<String> IMAGE_TYPES = new HashSet<String>(Arrays.asList(new String[] {
			"JPG", "JPEG", "PNG", "WEBP", "GIF", "ICO", "BMP", "TIFF", "TIF", "PSD", "PCX", "RAW", "CRW", "CR2", "NEF",
			"ORF", "RAF", "RW2", "RWL", "SRW", "ARW", "DNG", "X3F"
	}));

	private static final Set<String> VIDEO_TYPES = new HashSet<String>(Arrays.asList(new String[] {
			"MOV", "MP4", "M4V", "3G2", "3GP"
	}));
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Files to be rename path is empty.");
			System.exit(1);
		}
		Pattern pattern = Pattern.compile("^renamed_\\d{8}_\\d{6}.txt$");
		File path = new File(args[0]);
		File[] files = path.listFiles();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		File log = new File(path, "renamed_" + simpleDateFormat.format(new Date()) + ".txt");

		try (PrintWriter writer = new PrintWriter(log)) {
			for (File file : files) {
				String filename = file.getName();
				if (pattern.matcher(filename).matches()) {
					continue;
				}
				int lastDotPos = filename.lastIndexOf('.');
				if (lastDotPos < 0) {
					writer.println(filename + " not contains Extention.");
					continue;
				}

				String ext = filename.substring(lastDotPos + 1); 

				Metadata metadata = null;
				try {
					metadata = ImageMetadataReader.readMetadata(file);
				} catch (ImageProcessingException e) {
					
				}
				
				String model = null;
				String time = null;
				if (metadata != null) {
					ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
					if (directory != null) {
						Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
						if (date != null) {
							time = simpleDateFormat.format(date);
						}
						model = directory.getString(ExifSubIFDDirectory.TAG_MODEL);
					}
//					
//					System.out.println("==============" + filename + "==============");
//					for (Directory directory : metadata.getDirectories()) {
//						String dirName = directory.getName();
//						for (Tag tag : directory.getTags()) {
//					        System.out.println(String.format("[%s] - %s = %s",
//					            directory.getName(), tag.getTagName(), tag.getDescription()));
//					    }
//					    if (directory.hasErrors()) {
//					        for (String error : directory.getErrors()) {
//					            System.err.println(String.format("ERROR: %s", error));
//					        }
//					    }
//					}
				} 

				if (time == null) {
					time = simpleDateFormat.format(new Date(file.lastModified()));
				}
				
				String type;
								
				if (IMAGE_TYPES.contains(ext.toUpperCase())) {
					type = "IMG";
				} else if (VIDEO_TYPES.contains(ext.toUpperCase())) {
					type = "VIDEO";
				} else {
					type = "OTHER";
				}
								
				int count = 0;
				File newFile;
				do {
					String newFilename = type + "_" + time + (model != null ? "_" + model : "") + (count > 0 ? "_" + count : "") + "." + ext;
					newFile = new File(path, newFilename);
					if (filename.equals(newFilename)) {
						break;
					}
					
					count++;
				} while (newFile.exists());
				
				file.renameTo(newFile);
				
				String msg = filename + " -> " + newFile.getName();
				writer.println(msg);
				System.out.println(msg);
			}
		} finally {

		}

	}

}
