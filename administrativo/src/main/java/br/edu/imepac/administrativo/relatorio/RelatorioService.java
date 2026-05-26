package br.edu.imepac.administrativo.relatorio;

import br.edu.imepac.administrativo.integration.agendamento.AgendamentoClient;
import br.edu.imepac.administrativo.paciente.PacienteService;
import br.edu.imepac.administrativo.paciente.dto.PacienteResponse;
import br.edu.imepac.administrativo.relatorio.dto.ConsultaDiariaRelatorioResponse;
import br.edu.imepac.administrativo.relatorio.dto.PacientesPorConvenioRelatorioResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioService {

    private final AgendamentoClient agendamentoClient;
    private final PacienteService pacienteService;
    private final ModelMapper modelMapper;

    public RelatorioService(AgendamentoClient agendamentoClient,
                            PacienteService pacienteService,
                            ModelMapper modelMapper) {
        this.agendamentoClient = agendamentoClient;
        this.pacienteService = pacienteService;
        this.modelMapper = modelMapper;
    }

    // consulta o microsservico de agendamento (GET /v1/consultas?data=X) e conta o total.
    // se o agendamento estiver fora, AgendamentoClient propaga ServicoIndisponivelException (503).
    public ConsultaDiariaRelatorioResponse consultasDiarias(LocalDate data) {
        long total = agendamentoClient.contarConsultasPorData(data);
        return new ConsultaDiariaRelatorioResponse(data, (int) total);
    }

    public PacientesPorConvenioRelatorioResponse pacientesPorConvenio(Long convenioId) {
        List<PacienteResponse> pacientes = pacienteService.findByConvenioId(convenioId)
                .stream()
                .map(entity -> modelMapper.map(entity, PacienteResponse.class))
                .toList();
        return new PacientesPorConvenioRelatorioResponse(convenioId, pacientes.size(), pacientes);
    }
}
