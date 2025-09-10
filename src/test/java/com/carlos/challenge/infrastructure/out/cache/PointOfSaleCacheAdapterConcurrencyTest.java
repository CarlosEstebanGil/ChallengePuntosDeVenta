package com.carlos.challenge.infrastructure.out.cache;

import com.carlos.challenge.infrastructure.out.persistence.cache.adapter.PointOfSaleCacheAdapter;
import com.carlos.challenge.domain.model.PointOfSale;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

class PointOfSaleCacheAdapterConcurrencyTest {

    @Test
    void concurrentSaveAssignsUniqueCodesAndIds() throws Exception {
        PointOfSaleCacheAdapter cache = new PointOfSaleCacheAdapter();

        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int idx = i;
            tasks.add(() -> cache.save(new PointOfSale(null, "P" + idx, null)).id());
        }

        List<Future<String>> futures = pool.invokeAll(tasks);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        List<String> ids = new ArrayList<>();
        for (Future<String> f : futures) ids.add(f.get());

        assertThat(ids).doesNotContainNull();
        assertThat(ids).doesNotHaveDuplicates();

        var all = cache.findAll();
        assertThat(all).hasSize(threads);
        assertThat(all.stream().map(PointOfSale::code).distinct().count()).isEqualTo(threads);
    }
}
