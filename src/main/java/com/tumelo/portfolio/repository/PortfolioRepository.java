package com.tumelo.portfolio.repository;

import java.util.Optional;

import com.tumelo.portfolio.model.PortfolioDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PortfolioRepository
        extends MongoRepository<PortfolioDocument, ObjectId> {

    Optional<PortfolioDocument> findFirstByOrderByIdAsc();
}
