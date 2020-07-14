/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.valueset.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gov.nist.hit.vs.valueset.domain.Code;
import gov.nist.hit.vs.valueset.service.AphlService;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class AphlController {

	@Autowired
	AphlService aphlService;

	@ApiIgnore
	@RequestMapping(value = "/upload/aphl", method = RequestMethod.POST, produces = { "application/json" })
	public String singleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
			return "redirect:uploadStatus";
		}

		String[] fileInfo = file.getOriginalFilename().split("\\.");
		System.out.println(file.getOriginalFilename());
		System.out.println(fileInfo.length);
		if (fileInfo.length < 2) {
			return "Invalid file name. It must be as follows: program_YYYYMMDD.xlsx";
		}

		String fileName = fileInfo[0];
		String[] vsInfo = fileName.split("_");

		if (vsInfo.length != 2) {
			return "Invalid file name. It must be as follows: program_YYYYMMDD.xlsx";
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date date;
		try {
			date = formatter.parse(vsInfo[1]);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "Invalid file name. It must be as follows: program_YYYYMMDD.xlsx";
		}

		try {
			// Get the file and save it somewhere
			byte[] bytes = file.getBytes();
			Path path = Paths.get(file.getOriginalFilename());
			Files.write(path, bytes);

			FileInputStream excelFile = new FileInputStream(new File(file.getOriginalFilename()));
			Workbook workbook = new XSSFWorkbook(excelFile);
			Sheet datatypeSheet = workbook.getSheet("All value sets");
			if (datatypeSheet != null) {
				Map<String, Set<Code>> valuesetsMap = new HashMap<>();

				Iterator<Row> iterator = datatypeSheet.iterator();
				int i = 1;
				while (iterator.hasNext()) {
					if (i == 1) {
						Row currentRow = iterator.next();
						if (!currentRow.getCell(0).getStringCellValue().equals("Value Set Name")
								|| !currentRow.getCell(1).getStringCellValue().equals("Code")
								|| !currentRow.getCell(2).getStringCellValue().equals("CodeSystem")) {
							return "Invalid header";
						}
					} else {

						Row currentRow = iterator.next();
						Code c = new Code();
			
						try {

							c.setValue(currentRow.getCell(1).getStringCellValue());
							c.setCodeSystem(currentRow.getCell(2).getStringCellValue());
							if (valuesetsMap.containsKey(currentRow.getCell(0).getStringCellValue())) {
								valuesetsMap.get(currentRow.getCell(0).getStringCellValue()).add(c);
							} else {
								Set<Code> codes = new HashSet<Code>();
								codes.add(c);
								valuesetsMap.put(currentRow.getCell(0).getStringCellValue(), codes);
							}

						} catch (Exception e) {
							e.printStackTrace();
							return "Error in line: " +  Integer.toString(i) ;
						}
					}
					i++;
				}
				Files.deleteIfExists(path);
				System.out.println("Done");
				int addedVs = aphlService.saveValuesetsFromMap(vsInfo[0].toLowerCase(), date, valuesetsMap);
				return "Upload success. " + addedVs + " Valuesets added.";
			}
			return "Invalid file";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "File not found";
		} catch (IOException e) {
			e.printStackTrace();
			return "Invalid file";
		}
	}

}
