package propensi.b02.sobatarlydia.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import propensi.b02.sobatarlydia.model.KuantitasKey;
import propensi.b02.sobatarlydia.model.KuantitasModel;
import propensi.b02.sobatarlydia.model.ObatDetailModel;
import propensi.b02.sobatarlydia.model.ObatModel;
import propensi.b02.sobatarlydia.model.PenggunaModel;
import propensi.b02.sobatarlydia.model.PenjualanModel;
import propensi.b02.sobatarlydia.service.ObatDetailService;
import propensi.b02.sobatarlydia.service.ObatService;
import propensi.b02.sobatarlydia.service.PenjualanService;
import propensi.b02.sobatarlydia.service.UserService;
import propensi.b02.sobatarlydia.service.KuantitasService;

@Controller
@RequestMapping("/penjualan")
public class PenjualanController {
    
    @Autowired
    PenjualanService penjualanService;

    @Autowired
    KuantitasService kuantitasService;

    @Autowired
    ObatDetailService obatDetailService;

    @Autowired
    ObatService obatService;

    @Autowired
    UserService userService;

    @GetMapping("/viewall")
    public String viewAllPenjualan (Model model) {
        List<PenjualanModel> listPenjualan = penjualanService.getListPenjualan();
        model.addAttribute("listPenjualan", listPenjualan);
        return "viewall-penjualan";
    }

    @GetMapping("/add")
    public String addPenjualanFormPage(Model model){
        PenjualanModel penjualan = new PenjualanModel();
        penjualan.setKuantitas(new ArrayList<>());

        KuantitasModel kuantitas = new KuantitasModel();
        kuantitas.setKuantitas(1);
        penjualan.getKuantitas().add(kuantitas);
        
        List<ObatModel> daftarObat = obatService.getListObatDiterimaDanTersedia();
        model.addAttribute("daftarObat", daftarObat);

        model.addAttribute("penjualan", penjualan);
    
        return "form-add-penjualan";
    }

    @PostMapping(value = "/add", params = {"save"})
    public String addPenjualanSubmitPage(@ModelAttribute PenjualanModel penjualan, Model model, Principal principal) {
        PenggunaModel karyawan = userService.getAkunByEmail(principal.getName());
        penjualan.setKaryawan(karyawan);
        penjualan.setWaktu(LocalDateTime.now());

        List<KuantitasModel> listObat = penjualan.getKuantitas();
        if (penjualan.getKuantitas() == null) {
            penjualan.setKuantitas(new ArrayList<>());
        }

        penjualan.setHarga(0);
        penjualanService.addPenjualan(penjualan);

        int total = 0;
        for (int i=0; i < listObat.size(); i++) {
            String[] detail = listObat.get(i).getId().getObat().getObatDetailId().getIdObat().getNamaObat().split(" - ");
            ObatModel medicine = obatService.getObatByNamaDanFarmasi(detail[0], detail[1]);
            
            int kuantitas = listObat.get(i).getKuantitas();

            if (medicine.getListDetailObat().size() > 1) {
                int sisaStok = kuantitas;
                for (int j = 0; j < medicine.getListDetailObat().size(); j++) {
                    ObatDetailModel obatt = obatDetailService.getObatDetailByKodeBatch(medicine, medicine.getListDetailObat().get(j).getObatDetailId().getKodeBatch()); 
    
                    KuantitasKey key = new KuantitasKey(obatt, penjualan); 

                    if (medicine.getListDetailObat().get(j).getStokTotal() - sisaStok >= 0) {
                        obatt.setStokTotal(obatt.getStokTotal() - sisaStok);
                        listObat.set(i, listObat.get(i));
                        listObat.get(i).setId(key);
                        listObat.get(i).setKuantitas(sisaStok);
                        kuantitasService.addKuantitas(listObat.get(i));
                        break;
                    } else {
                        sisaStok -= obatt.getStokTotal();
                        int quantity = obatt.getStokTotal();

                        obatt.setStokTotal(0);
                        obatt.setStatus("Kosong");

                        KuantitasModel qty = new KuantitasModel();
                        listObat.set(i, qty);
                        listObat.get(i).setId(key);
                        listObat.get(i).setKuantitas(quantity);
                        kuantitasService.addKuantitas(listObat.get(i));
                    }
                }
            } else {
                ObatDetailModel med = obatDetailService.getObatDetailByIdObat(medicine);   

                kuantitas = listObat.get(i).getKuantitas();
                med.setStokTotal(med.getStokTotal() - kuantitas);        
    
                listObat.set(i, listObat.get(i));
    
                KuantitasKey key = new KuantitasKey(med, penjualan);
                listObat.get(i).setId(key);
                listObat.get(i).setKuantitas(kuantitas);
                kuantitasService.addKuantitas(listObat.get(i));
            }
            total += medicine.getHarga() * kuantitas;
        }   

        penjualan.setHarga(total);
        penjualan.setKuantitas(listObat);
        penjualanService.addPenjualan(penjualan);

        return "redirect:/penjualan/viewall";
    }

    @PostMapping(value = "/add", params = {"addRowObat"})
    public String addRowPenjualanMultiple(@ModelAttribute PenjualanModel penjualan, Model model) {
        if (penjualan.getKuantitas() == null || penjualan.getKuantitas().size() == 0) {
            penjualan.setKuantitas(new ArrayList<>());
        }
        
        List<ObatModel> daftarObat = obatService.getListObatDiterimaDanTersedia();

        KuantitasModel kuantitas = new KuantitasModel();
        kuantitas.setKuantitas(1);
        penjualan.getKuantitas().add(kuantitas);
        List<KuantitasModel> listObat = penjualan.getKuantitas();
      
        model.addAttribute("daftarObat", daftarObat);
        model.addAttribute("penjualan", penjualan);
        model.addAttribute("listObat", listObat);
      
        return "form-add-penjualan";
    }

    @PostMapping(value = "/add", params = {"deleteRowObat"})
    public String deleteRowPenjualanMultiple(@ModelAttribute PenjualanModel penjualan, @RequestParam("deleteRowObat") Integer row, Model model) {
        final Integer rowId = Integer.valueOf(row);
        penjualan.getKuantitas().remove(rowId.intValue());

        List<KuantitasModel> listObat = penjualan.getKuantitas();
        List<ObatModel> daftarObat = obatService.getListObatDiterimaDanTersedia();
       
        model.addAttribute("daftarObat", daftarObat);
        model.addAttribute("penjualan", penjualan);
        model.addAttribute("listObat", listObat);

        return "form-add-penjualan";
    }

    @GetMapping("/detail-penjualan/{idPenjualan}")
    public String detailPenjualan(Model model, @PathVariable Long idPenjualan){
        PenjualanModel penjualan = penjualanService.getPenjualanById(idPenjualan);
        if (penjualan==null) {
            return "viewall-penjualan";
        }
        
        List<KuantitasModel> quantity = new ArrayList<>();
        for (int i = penjualan.getKuantitas().size()-1; i > 0; i--) {
            if (penjualan.getKuantitas().get(i).getId().getObat().getObatDetailId().getIdObat().getIdObat().equals(penjualan.getKuantitas().get(i-1).getId().getObat().getObatDetailId().getIdObat().getIdObat())) {
                penjualan.getKuantitas().get(i-1).setKuantitas(penjualan.getKuantitas().get(i).getKuantitas() + penjualan.getKuantitas().get(i-1).getKuantitas());
                quantity.add(penjualan.getKuantitas().get(i));
            }
        }
       
        penjualan.getKuantitas().removeAll(quantity);
        model.addAttribute("detailPenjualan", penjualan);
        return "viewall-detail-penjualan";
    }
}