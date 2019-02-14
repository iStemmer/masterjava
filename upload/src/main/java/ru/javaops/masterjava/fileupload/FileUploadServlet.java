package ru.javaops.masterjava.fileupload;

import com.google.common.base.Splitter;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Strings.nullToEmpty;

@WebServlet(name = "FileUploadServlet", urlPatterns = {"/upload"})
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
    private final static Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getCanonicalName());
    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Create path components to save the file
        final String path = request.getParameter("destination");
        final Part filePart = request.getPart("file");
        final String fileName = getFileName(filePart);

        //InputStream filecontent = null;
        final PrintWriter writer = response.getWriter();

        try (InputStream filecontent = filePart.getInputStream()) {
            StaxStreamProcessor processor = new StaxStreamProcessor(filecontent);
            final Set<String> groupNames = new HashSet<>();

            // Users loop
            Set<User> users = new TreeSet<>(USER_COMPARATOR);

            JaxbParser parser = new JaxbParser(User.class);
            while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                String groupRefs = processor.getAttribute("groupRefs");
                if (!Collections.disjoint(groupNames, Splitter.on(' ').splitToList(nullToEmpty(groupRefs)))) {
                    User user = parser.unmarshal(processor.getReader(), User.class);
                    users.add(user);
                }
            }
            System.out.println("test2");
            //print users, ho ho ho ho ho(!!!)
            writer.println("<table>");
            users.forEach(user -> {
                writer.println("<tr>");
                writer.println("<td>" + user.getValue() + "</td>");
                writer.println("<td>" + user.getEmail() + "</td>");
                writer.println("<td>" + user.getFlag() + "</td>");
                writer.println("</tr>");
                System.out.println("Test");

            });
            writer.println("</table");
        } catch (FileNotFoundException fne) {
            writer.println("You either did not specify a file to upload or are "
                    + "trying to upload a file to a protected or nonexistent "
                    + "location.");
            writer.println("<br/> ERROR: " + fne.getMessage());

            LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                    new Object[]{fne.getMessage()});
        } catch (XMLStreamException xmlException) {
            LOGGER.log(Level.SEVERE, "Problems while XML file parsing");
            writer.println("Problems while xml file parsing");
        } catch (JAXBException e) {
            LOGGER.log(Level.SEVERE, "Problems during file parsing");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        LOGGER.log(Level.INFO, "Part Header = {0}", partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                String fileName = content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf("\\") + 1);
            }
        }
        return null;
    }
}
