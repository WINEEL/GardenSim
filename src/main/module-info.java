module com.gardensim {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires org.apache.logging.log4j;
    requires org.json;

    opens com.gardensim to javafx.fxml;
    exports com.gardensim;
}
