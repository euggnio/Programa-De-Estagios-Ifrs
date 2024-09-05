package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;


import java.awt.Color;
import java.io.IOException;
import java.util.List;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
public class PdfGenerator {

    private List<Estagiarios> estagiariosList;
    private List<Servidor> servidores;

    public PdfGenerator(List<Estagiarios> estagiariosList, List<Servidor> servidores) {
        this.estagiariosList = estagiariosList;
        this.servidores = servidores;
    }

    public Document pegarPdfEstagiarios( HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(16);
        font.setColor(Color.blue);
        Paragraph p = new Paragraph("Lista de estagiários", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);
        PdfPTable table = new PdfPTable(12);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10);
        gerarHeader(table);
        gerarDados(table);
        document.add(table);
        document.close();
        return document;
    }

private void gerarDados(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setSize(8);
        PdfPCell cell = new PdfPCell();
        boolean par = false;
        for (Estagiarios estagiario : estagiariosList) {
            if (par) {
                cell.setBackgroundColor(Color.getHSBColor(0, 0, 0.95f));
                par = false;
            } else {
                cell.setBackgroundColor(Color.getHSBColor(0, 0, 0.85f));
                par = true;
            }
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getAluno().getNomeCompleto(), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getAluno().getMatricula(), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getAluno().getUsuarioSistema().getEmail(), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getAluno().getCurso().getNomeCurso(), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(pegarNomeCoordenador(estagiario.getSolicitacao().getCurso().getId()), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getNomeEmpresa(), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getContatoEmpresa(), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getAgente(), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(String.valueOf(estagiario.getSolicitacao().getInicioDataEstagio()), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(String.valueOf(estagiario.getSolicitacao().getFinalDataEstagio()), font));
            table.addCell(cell);
            cell.setPhrase(new Phrase(estagiario.getSolicitacao().getTurnoEstagio(), font));
            table.addCell(cell);
            String status;
            if(estagiario.isAtivo()){
                status = pegarStatus(estagiario.getSolicitacao().getInicioDataEstagio(), estagiario.getSolicitacao().getFinalDataEstagio());
            }else{
                status = "Cancelado";
            }
            cell.setPhrase(new Phrase(status, font));
            table.addCell(cell);
        }
    }

    private String pegarStatus(LocalDate dataInicio, LocalDate dataFim){
        if(dataInicio.isAfter(LocalDate.now())){
            return "Aguardando início";
        }else if(dataFim.isBefore(LocalDate.now())){
            return "Terminado";
        }
        else if(dataInicio.isBefore(LocalDate.now()) && dataFim.isAfter(LocalDate.now())){
            return "Em andamento";
        }
        return "";
    }

    private void gerarHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.DARK_GRAY);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);
        font.setSize(11);

        //cell.setPhrase(new Phrase("ID", font));
        //table.addCell(cell);

        cell.setPhrase(new Phrase("Nome", font));
        table.addCell(cell);

        //cell.setPhrase(new Phrase("Drive", font));
        //table.addCell(cell);

        cell.setPhrase(new Phrase("Matrícula", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("E-mail", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Orientador", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Curso", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Empresa", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Contato empresa", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Agente integrador", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Início", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Fim", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Turno", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Status", font));
        table.addCell(cell);
    }


    private String pegarNomeCoordenador(Long id){
        for(Servidor servidor : servidores){
            if(servidor.getCurso().getId() == id){
                return servidor.getNome();
            }
        }
        return "";
    }
}
