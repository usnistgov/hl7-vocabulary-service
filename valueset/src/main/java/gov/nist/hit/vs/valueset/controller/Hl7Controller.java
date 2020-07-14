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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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
import gov.nist.hit.vs.valueset.service.Hl7Service;
import gov.nist.hit.vs.valueset.service.ValuesetService;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class Hl7Controller {

	@Autowired
	Hl7Service hl7Service;

	@Autowired
	ValuesetService valuesetService;

	@ApiIgnore
	@RequestMapping(value = "/upload/hl7", method = RequestMethod.POST, produces = { "application/json" })
	public String singleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
			return "redirect:uploadStatus";
		}

		try {
			// Get the file and save it somewhere
			byte[] bytes = file.getBytes();
			Path path = Paths.get(file.getOriginalFilename());
			Files.write(path, bytes);

			FileInputStream excelFile = new FileInputStream(new File(file.getOriginalFilename()));
			Workbook workbook = new XSSFWorkbook(excelFile);
			Sheet tableSheet = workbook.getSheet("HL70396");
			if (tableSheet != null) {
				Set<Code> codes = new HashSet<Code>();

				Iterator<Row> iterator = tableSheet.iterator();
				int i = 1;
				while (iterator.hasNext()) {
					if (i == 1) {
						Row currentRow = iterator.next();
//						if (!currentRow.getCell(0).getStringCellValue().equals("Value Set Name")
//								|| !currentRow.getCell(1).getStringCellValue().equals("Code")
//								|| !currentRow.getCell(2).getStringCellValue().equals("CodeSystem")) {
//							return "Invalid header";
//						}
					} else {
						Row currentRow = iterator.next();
						Code c = new Code();
						c.setValue(extractStringFromCell(currentRow, 0));
						c.setDisplay(extractStringFromCell(currentRow, 1));
						c.setDefinition(extractStringFromCell(currentRow, 2));
						c.setV2TableStatus(extractStringFromCell(currentRow, 3));
						c.setDeprecated(String.valueOf(extractNumberFromCell(currentRow, 4)));
						c.setV2ConceptComment(extractStringFromCell(currentRow, 5));
						c.setV2ConceptCommentAsPublished(extractStringFromCell(currentRow, 6));
						c.setCodeSystem(extractStringFromCell(currentRow, 7));
						c.setCodeType(extractStringFromCell(currentRow, 8));
						c.setRegexRule(extractStringFromCell(currentRow, 9));
						c.setComments(extractStringFromCell(currentRow, 10));
						c.setExclude(extractStringFromCell(currentRow, 11) == null ? false
								: extractStringFromCell(currentRow, 11).equals("exclude"));
						codes.add(c);
					}
					i++;
				}
				Files.deleteIfExists(path);
				System.out.println("Done");
				hl7Service.saveTable("HL70396", "2.x", codes);
				return "Upload success. ";
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

	private String extractStringFromCell(Row row, int i) {
		return row.getCell(i) != null ? row.getCell(i).getStringCellValue() : null;
	}

	private Double extractNumberFromCell(Row row, int i) {
		return row.getCell(i) != null ? row.getCell(i).getNumericCellValue() : null;
	}
}
