//   This program is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package h2xml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Convert {

	public static String readTextFile(String fullPathFilename)
			throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(
				fullPathFilename));

		char[] chars = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(chars)) > -1) {
			sb.append(String.valueOf(chars, 0, numRead));
		}

		reader.close();

		return sb.toString();
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		BufferedReader test = null;

		String filename = args[0]; // lm3s8962.h
		String txt_filename = args[1]; // lm3s8962.txt
		try {
			test = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line;
		String group = "";
		String groupdescription = "";
		String registergroup = "";
		String registername = "";
		String address = "";
		int start = 0;
		int blcomment = 0;

		String chipname = filename.substring(0, filename.length() - 2)
				.toUpperCase();


		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(true);

		Document doc = new Document();

		Element root = new Element("model");
		root.setAttribute("chipname", chipname);
		doc.addContent(root);

		DocType doctype = new DocType("model", "embsysregview.dtd");
		doc.setDocType(doctype);

		try {
			String chip_description_text = readTextFile(txt_filename);
			Element chip_description = new Element("chip_description");
			chip_description.setText(chip_description_text);
			root.addContent(chip_description);
		} catch (IOException e) {
		}

		try {
			while ((line = test.readLine()) != null) {
				if (start == 1) {
					if (line.startsWith("#define")
							&& line.contains("volatile unsigned long")) {
						// Register erkannt ...
						// Adresse ausschneiden
						int s1 = line.indexOf(")");
						address = line.substring(s1 + 1, s1 + 11);

						// Registername ausschneiden
						s1 = line.indexOf("(");
						registername = line.substring(8, s1 - 1).trim();

						// Registergroup
						s1 = registername.indexOf("_");
						// nummer wegschneiden ...
						char c1 = registername.charAt(s1 - 1);
						if (c1 >= '0' && c1 <= '9')
							s1--;

						String newgroup = registername.substring(0, s1);
						if (!newgroup.equals(group))
							group = newgroup;

						Element element_group = null;
						List<Element> elements_group = (List<Element>) root
								.getChildren("group");
						for (Element el_group : elements_group) {
							if (el_group.getAttribute("name").getValue()
									.equals(group)) {
								element_group = el_group;
								break;
							}
						}
						if (element_group == null) {
							element_group = new Element("group");
							element_group.setAttribute("name", group);
							element_group.setAttribute("description",
									groupdescription);
							root.addContent(element_group);
						}

						Element element_registergroup = null;
						List<Element> elements_registergroup = element_group
								.getChildren("registergroup");
						for (Element el_registergroup : elements_registergroup) {
							if (el_registergroup.getAttribute("name")
									.getValue().equals(registergroup)) {
								element_registergroup = el_registergroup;
								break;
							}
						}
						if (element_registergroup == null) {
							element_registergroup = new Element("registergroup");
							element_registergroup.setAttribute("name",
									registergroup);
							element_registergroup.setAttribute("description",
									"");
							element_group.addContent(element_registergroup);
						}
						Element element_register = new Element("register");
						element_register.setAttribute("name", registername);
						element_register.setAttribute("description", "");
						element_register.setAttribute("address", address);
						element_register.setAttribute("access", "rw");
						element_registergroup.addContent(element_register);
					}

					if (blcomment == 2) {
						if (!line.equals("//") && line.startsWith("//")) {
							int s1 = line.indexOf("(");
							int s2 = line.indexOf(")");
							if (s1 != -1 && s2 != -1) {
								groupdescription = line.substring(3, s1 - 1);
								registergroup = line.substring(s1 + 1, s2);
							}
						}
						blcomment = 0;
					}

					if (blcomment == 1) {
						if (line.equals("//"))
							blcomment = 2;
						else
							blcomment = 0;
					}
					if (line.contains("//*************"))
						blcomment = 1;
				} else if (line.contains("#ifndef"))
					start = 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		try {
			out.output(doc, System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
