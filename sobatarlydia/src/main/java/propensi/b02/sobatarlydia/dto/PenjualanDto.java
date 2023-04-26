package propensi.b02.sobatarlydia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PenjualanDto {
    @JsonProperty("kode")
    private long kode;
    
    @JsonProperty("tanggal")
    private String tanggal;

    @JsonProperty("waktu")
    private String waktu;

    @JsonProperty("karyawan")
    private String karyawan;

    @JsonProperty("harga")
    private String harga;
}
