package sr.we;

import jakarta.annotation.PostConstruct;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeMappingResultor;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageConnectionFoundation;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageFoundation;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

@Controller
@EnableScheduling
public class ApplicationController {

    private EmbeddedStorageManager embeddedStorage;

    public ApplicationController(EmbeddedStorageManager embeddedStorage) {
        this.embeddedStorage = embeddedStorage;


    }
}
