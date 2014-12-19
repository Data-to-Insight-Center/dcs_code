package org.dataconservancy.registry.http;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.dataconservancy.dcs.spring.mvc.BaseView;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.api.support.RegistryEntry;
import org.dataconservancy.registry.impl.license.shared.DcsLicense;
import org.dataconservancy.registry.impl.license.shared.LicenseConverter;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeConverter;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Renders a RegistryEntry as XML.
 */
public class RegistryEntryView implements View {

    private final int bufSize = 4 * 1024;

    private final XStream xstream;

    public RegistryEntryView() {
        this.xstream = new XStream(new StaxDriver());

        // TODO: mechanisms for registry instance implementations to register their own XStream aliases and Converters.
        this.xstream.alias("entry", BasicRegistryEntryImpl.class);
        this.xstream.alias("license", DcsLicense.class);
        this.xstream.alias("metadataScheme", DcsMetadataScheme.class);

        this.xstream.registerConverter(new LicenseConverter());
        this.xstream.registerConverter(new MetadataSchemeConverter());
        this.xstream.registerConverter(new RegistryEntryConverter());
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse res) throws Exception {
        res.setStatus(HttpServletResponse.SC_OK);

        RegistryEntry entry = ((RegistryEntryHolder)model.get(RegistryModelAttribute.ENTRY.name())).entry;
        ByteArrayOutputStream content = new ByteArrayOutputStream(bufSize);
        xstream.toXML(entry.getEntry(), content);

        res.setContentLength(content.size());

        OutputStream out = res.getOutputStream();
        content.writeTo(out);
        out.flush();
        out.close();
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }
}
