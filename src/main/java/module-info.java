module com.xuka.exam {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    // Hibernate and Jakarta Persistence
    requires transitive jakarta.persistence;
    requires org.hibernate.orm.core;

    // Logging
    requires org.slf4j;

    opens com.xuka.exam to javafx.fxml;
    opens com.xuka.exam.controller to javafx.fxml;
    opens com.xuka.exam.models to org.hibernate.orm.core;
    opens com.xuka.exam.config to org.hibernate.orm.core;
    
    exports com.xuka.exam;
    exports com.xuka.exam.models;
    exports com.xuka.exam.config;
    exports com.xuka.exam.dao;
    exports com.xuka.exam.controller;
}