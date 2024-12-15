package cleaningscheduler.api.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CustomObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public CustomObjectMapperProvider() {
        // Set up and configure the ObjectMapper with JodaModule
        mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
