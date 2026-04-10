package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.Data.Service.ViaCep.ApiViaCep;
import br.fmu.projetoasthmaspace.Core.Util.AtualizarRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Cliente.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Core.Domain.Endereco.Endereco;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiClient;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiService;
import br.fmu.projetoasthmaspace.Data.Service.ViaCep.ViaCepResponse;
import br.fmu.projetoasthmaspace.databinding.ActivityInformacoesPessoaisBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformacoesPessoaisActivity extends AppCompatActivity {

    private ActivityInformacoesPessoaisBinding binding;
    private ApiService api;
    private DadosDetalhamentoCliente dadosAtuais;
    private boolean carregandoDados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformacoesPessoaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = ApiClient.getApiService(getApplicationContext());

        Log.d("INFO_PESSOAIS", "Tela aberta — sincronizando com backend");

        carregarDadosBackend();

        binding.edtCEP.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !carregandoDados) {
                String cep = binding.edtCEP.getText().toString().trim();
                if (cep.matches("\\d{8}")) {
                    buscarCep(cep);
                }
            }
        });

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnSalvar.setOnClickListener(v -> salvarAlteracoes());
    }

    private void carregarDadosBackend() {
        api.getMeuPerfil().enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call,
                                   Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dadosAtuais = response.body();
                    atualizarUI(dadosAtuais);
                } else {
                    Toast.makeText(InformacoesPessoaisActivity.this,
                            "Erro ao buscar dados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Log.e("INFO_PESSOAIS", "Falha: " + t.getMessage());
                Toast.makeText(InformacoesPessoaisActivity.this,
                        "Falha na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarUI(DadosDetalhamentoCliente d) {
        carregandoDados = true;

        binding.edtNome.setText(safe(d.getNome()));
        binding.txtEmail.setText(safe(d.getEmail()));
        binding.edtTelefone.setText(safe(d.getTelefone()));
        binding.txtCPF.setText(safe(d.getCpf()));
        binding.edtIdade.setText(safeInt(d.getIdade()));
        binding.edtMedicamentos.setText(safe(d.getMedicamentos()));
        binding.edtProblemaResp.setText(safe(d.getProblema_respiratorio()));
        binding.edtEmergencia.setText(safe(d.getContatoEmergencia()));

        // Seleciona o sexo no Spinner
        String sexo = safe(d.getSexo());
        String[] opcoesSexo = getResources().getStringArray(R.array.spinner_sexo);
        for (int i = 0; i < opcoesSexo.length; i++) {
            if (opcoesSexo[i].equalsIgnoreCase(sexo)) {
                binding.spinnerSexo.setSelection(i);
                break;
            }
        }

        if (d.getEndereco() != null) {
            binding.edtCEP.setText(safe(d.getEndereco().getCep()));
            binding.edtLogradouro.setText(safe(d.getEndereco().getLogradouro()));
            binding.edtNumero.setText(safe(d.getEndereco().getNumero()));
            binding.edtComplemento.setText(safe(d.getEndereco().getComplemento()));
            binding.edtBairro.setText(safe(d.getEndereco().getBairro()));
            binding.edtCidade.setText(safe(d.getEndereco().getCidade()));
            binding.edtUF.setText(safe(d.getEndereco().getUf()));
        }

        carregandoDados = false;
    }

    private void salvarAlteracoes() {
        if (dadosAtuais == null) return;

        String cep         = nullIfEmpty(binding.edtCEP.getText().toString());
        String logradouro  = nullIfEmpty(binding.edtLogradouro.getText().toString());
        String bairro      = nullIfEmpty(binding.edtBairro.getText().toString());
        String cidade      = nullIfEmpty(binding.edtCidade.getText().toString());
        String uf          = nullIfEmpty(binding.edtUF.getText().toString());
        String numero      = nullIfEmpty(binding.edtNumero.getText().toString());
        String complemento = nullIfEmpty(binding.edtComplemento.getText().toString());

        boolean algumPreenchido = cep != null || logradouro != null || bairro != null || cidade != null || uf != null;
        boolean todosObrigatoriosPreenchidos = cep != null && logradouro != null && bairro != null && cidade != null && uf != null;

        if (algumPreenchido && !todosObrigatoriosPreenchidos) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios do endereço", Toast.LENGTH_LONG).show();
            return;
        }

        if (cep != null && !cep.matches("\\d{8}")) {
            Toast.makeText(this, "CEP deve ter 8 dígitos sem traço", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSalvar.setEnabled(false);
        binding.btnSalvar.setText("Salvando...");


        String sexoSelecionado = binding.spinnerSexo.getSelectedItem().toString();

        AtualizarRequest request = new AtualizarRequest();
        request.nome                  = nullIfEmpty(binding.edtNome.getText().toString());
        request.telefone              = nullIfEmpty(binding.edtTelefone.getText().toString());
        request.sexo                  = nullIfEmpty(sexoSelecionado);
        request.idade                 = parseInt(binding.edtIdade.getText().toString().trim());
        request.medicamentos          = nullIfEmpty(binding.edtMedicamentos.getText().toString());
        request.contatoEmergencia     = nullIfEmpty(binding.edtEmergencia.getText().toString());
        request.problema_respiratorio = nullIfEmpty(binding.edtProblemaResp.getText().toString());

        if (algumPreenchido) {
            Endereco end = new Endereco();
            end.setCep(cep);
            end.setLogradouro(logradouro);
            end.setNumero(numero);
            end.setComplemento(complemento);
            end.setBairro(bairro);
            end.setCidade(cidade);
            end.setUf(uf);
            request.endereco = end;
        } else {
            request.endereco = null;
        }

        api.atualizarPerfil(request).enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call,
                                   Response<DadosDetalhamentoCliente> response) {
                binding.btnSalvar.setEnabled(true);
                binding.btnSalvar.setText("Salvar Alterações");

                if (response.isSuccessful() && response.body() != null) {
                    dadosAtuais = response.body();
                    atualizarUI(dadosAtuais);
                    Toast.makeText(InformacoesPessoaisActivity.this,
                            "✓ Alterações salvas!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("INFO_PESSOAIS", "Erro HTTP: " + response.code());
                    Toast.makeText(InformacoesPessoaisActivity.this,
                            "Erro ao salvar alterações", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                binding.btnSalvar.setEnabled(true);
                binding.btnSalvar.setText("Salvar Alterações");
                Log.e("INFO_PESSOAIS", "Falha: " + t.getMessage());
                Log.e("INFO_PESSOAIS", "Causa: " + t.getCause());
                Toast.makeText(InformacoesPessoaisActivity.this,
                        "Falha na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarCep(String cep) {
        ApiViaCep.getService().buscarCep(cep).enqueue(new Callback<ViaCepResponse>() {
            @Override
            public void onResponse(Call<ViaCepResponse> call, Response<ViaCepResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ViaCepResponse dados = response.body();
                    if ("true".equals(dados.erro)) {
                        Toast.makeText(InformacoesPessoaisActivity.this, "CEP não encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    binding.edtLogradouro.setText(dados.logradouro);
                    binding.edtBairro.setText(dados.bairro);
                    binding.edtCidade.setText(dados.localidade);
                    binding.edtUF.setText(dados.uf);
                    if (dados.complemento != null && !dados.complemento.isEmpty()) {
                        binding.edtComplemento.setText(dados.complemento);
                    }
                    binding.edtNumero.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<ViaCepResponse> call, Throwable t) {
                Toast.makeText(InformacoesPessoaisActivity.this, "Erro ao buscar CEP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String nullIfEmpty(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
    }

    private String safeInt(Integer v) {
        return (v == null || v == 0) ? "" : String.valueOf(v);
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return null; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dadosAtuais = null;
        binding = null;
        api = null;
        Log.d("INFO_PESSOAIS", "Dados destruídos ao sair da tela");
    }
}