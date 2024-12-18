package propensi.b02.sobatarlydia.service;

import propensi.b02.sobatarlydia.model.FakturModel;

import java.time.LocalDate;
import java.util.List;

public interface FakturService {
    public List<FakturModel> getFakturByFarmasi(String farmasi);
    public FakturModel getFakturByFarmasiDanTanggal(List<FakturModel> fakturs, LocalDate tanggal);
    public int generateKodeBatch(List<FakturModel> faktur);
    public void add(FakturModel faktur);
    public FakturModel getFakturByNo(String noFaktur);
    public FakturModel updateFakturStatus(FakturModel fakturObat);
    public List<FakturModel> getAllFaktur();
}
