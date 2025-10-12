module backup.agent {
    requires javafx.controls;
    requires atlantafx.base;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.data.jpa;
    requires spring.data.commons;

    requires jakarta.persistence;
    requires jakarta.annotation;
    requires org.hibernate.orm.core;

    requires java.sql;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires spring.tx;
    requires jakarta.validation;
    requires java.logging;

    opens org.iclassq;
    opens org.iclassq.controller;
    opens org.iclassq.service;
    opens org.iclassq.repository;
    opens org.iclassq.entity;
    opens org.iclassq.views;
    opens org.iclassq.views.components;
    opens org.iclassq.enums;
    opens org.iclassq.utils;
    opens org.iclassq.validation;

    exports org.iclassq;
    exports org.iclassq.entity;
    exports org.iclassq.views;
    exports org.iclassq.views.components;
    exports org.iclassq.controller;
    exports org.iclassq.service;
    exports org.iclassq.repository;
    exports org.iclassq.enums;
    exports org.iclassq.utils;
}