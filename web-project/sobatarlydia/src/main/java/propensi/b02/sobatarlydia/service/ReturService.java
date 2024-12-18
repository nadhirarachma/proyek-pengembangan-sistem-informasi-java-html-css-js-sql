package propensi.b02.sobatarlydia.service;
import org.springframework.stereotype.Service;
import propensi.b02.sobatarlydia.dto.ReturUpdtDto;

import propensi.b02.sobatarlydia.model.ReturObatModel;

import java.util.List;

@Service
public interface ReturService {

    ReturObatModel getReturById(Long returId);
    ReturUpdtDto makeReturUpdateDTO(ReturObatModel retur);
    void add(ReturObatModel retur);
    List<ReturObatModel> getListRetur();
    List<ReturObatModel> getListReturForKaryawan();
    List<ReturObatModel> getListReturForApoteker();
    ReturObatModel verifikasiRetur(ReturObatModel returObat, String status);
}
