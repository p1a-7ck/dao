package dao;

import java.util.List;

/**
 * dao
 */
public interface Dao<T> {
    List<T> findById(Long id);
}
