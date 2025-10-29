module br.com.undb.visionclass.visionclassdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.csv;

    opens br.com.undb.visionclass.visionclassdesktop to javafx.fxml;
    opens br.com.undb.visionclass.visionclassdesktop.model to javafx.base;

    exports br.com.undb.visionclass.visionclassdesktop;
}