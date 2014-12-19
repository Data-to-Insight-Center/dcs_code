package org.dataconservancy.registry.http;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Renders URL references.  The URLs may reference registries, or individual registry entries.
 */
public class EntryReferencesView implements View {

    private final int bufSize = 1024 * 4;

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse res) throws Exception {
        @SuppressWarnings("unchecked")
        ReferencesHolder refHolder = (ReferencesHolder) model.get(RegistryModelAttribute.REFS.name());
        Set<URL> entryRefs = refHolder.references;
        int statusCode = refHolder.statusCode;

        ByteArrayOutputStream content = new ByteArrayOutputStream(bufSize);
        for (URL ref : entryRefs) {
            content.write(ref.toExternalForm().getBytes());
            content.write("\n".getBytes());
        }

        if (entryRefs.size() == 1) {
            res.setHeader("Location", entryRefs.iterator().next().toExternalForm());
        }

        res.setStatus(statusCode);

        res.setContentLength(content.size());

        OutputStream out = res.getOutputStream();
        content.writeTo(out);
        out.flush();
        out.close();
    }

}
