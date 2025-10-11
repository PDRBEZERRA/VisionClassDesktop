module br.com.undb.visionclass.visionclassdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    opens br.com.undb.visionclass.visionclassdesktop to javafx.fxml;
    opens br.com.undb.visionclass.visionclassdesktop.model to javafx.base;

    exports br.com.undb.visionclass.visionclassdesktop;
}