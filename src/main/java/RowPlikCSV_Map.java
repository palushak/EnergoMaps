/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Jacek Paluszak <palsoft.pl>
 */
final class RowPlikCSV_Map extends RecursiveTreeObject<RowPlikCSV_Map> {

    final StringProperty id_punktu;
    final StringProperty miejscowosc;
    final StringProperty ulica;
    final StringProperty budynek;
    final StringProperty licznik;
    final StringProperty kod_punktu;
    final StringProperty cykl;
    final StringProperty faza;
    final StringProperty grupa;

    RowPlikCSV_Map(
            String id_punktu,
            String miejscowosc,
            String ulica,
            String budynek,
            String licznik,
            String kod_punktu,
            String cykl,
            String faza,
            String grupa

    ) {
        this.id_punktu = new SimpleStringProperty(id_punktu);
        this.miejscowosc = new SimpleStringProperty(miejscowosc);
        this.ulica = new SimpleStringProperty(ulica);
        this.budynek = new SimpleStringProperty(budynek);
        this.licznik = new SimpleStringProperty(licznik);
        this.kod_punktu = new SimpleStringProperty(kod_punktu);
        this.cykl = new SimpleStringProperty(cykl);
        this.faza = new SimpleStringProperty(faza);
        this.grupa = new SimpleStringProperty(grupa);

    }
}
