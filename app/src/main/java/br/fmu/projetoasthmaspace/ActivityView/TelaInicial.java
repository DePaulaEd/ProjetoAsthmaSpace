package br.fmu.projetoasthmaspace.ActivityView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import br.fmu.projetoasthmaspace.Domain.AirQualityUtils;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Service.AirResponse;
import br.fmu.projetoasthmaspace.Service.ApiOpenWeather;
import br.fmu.projetoasthmaspace.Service.ApiServiceOpenWeather;
import br.fmu.projetoasthmaspace.databinding.ActivityTelaInicialBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaInicial extends Fragment {

    private static final String TAG = "TelaInicial";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ActivityTelaInicialBinding binding;
    private PoluenteAdapter adapter;
    private List<Poluente> poluentes = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView chamado");
        binding = ActivityTelaInicialBinding.inflate(inflater, container, false);

        // Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated chamado");

        criarLinkEducativo();
        setupRecycler();
        verificarPermissoesECarregar();
    }

    // ---------------- Recycler ----------------

    private void setupRecycler() {
        Log.d(TAG, "setupRecycler iniciado");
        adapter = new PoluenteAdapter(poluentes);
        binding.recyclerPoluentes.setLayoutManager(
                new GridLayoutManager(getContext(), 2)
        );
        binding.recyclerPoluentes.setAdapter(adapter);
        Log.d(TAG, "RecyclerView configurado. Itens iniciais: " + poluentes.size());
    }

    // ---------------- UI fixa ----------------

    private void criarLinkEducativo() {
        String textoCompleto = "Não entende o que esses dados significam? Saiba mais.";
        SpannableString spannableString = new SpannableString(textoCompleto);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                FragmentManager fm = getParentFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.frameLayout, new Educativo());
                ft.addToBackStack(null);
                ft.commit();
            }
        };

        String textoLink = "Saiba mais.";
        int inicio = textoCompleto.indexOf(textoLink);
        int fim = inicio + textoLink.length();

        spannableString.setSpan(clickableSpan, inicio, fim,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.textLinkEducativo.setText(spannableString);
        binding.textLinkEducativo.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // ---------------- Permissões e Localização ----------------

    private void verificarPermissoesECarregar() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            obterLocalizacaoECarregar();
        } else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacaoECarregar();
            } else {
                Toast.makeText(getContext(),
                        "Permissão de localização negada. Usando São Paulo como padrão.",
                        Toast.LENGTH_LONG).show();
                carregarDadosReais(-23.5505, -46.6333, "São Paulo, SP");
            }
        }
    }

    private void obterLocalizacaoECarregar() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            Log.d(TAG, "Localização obtida: " + lat + ", " + lon);

                            // Aqui você pode usar um Geocoder para obter o nome da cidade
                            // Por enquanto, vamos usar "Sua Localização"
                            carregarDadosReais(lat, lon, "Sua Localização");
                        } else {
                            Log.w(TAG, "Localização null, usando São Paulo");
                            Toast.makeText(getContext(),
                                    "Não foi possível obter sua localização. Usando São Paulo.",
                                    Toast.LENGTH_SHORT).show();
                            carregarDadosReais(-23.5505, -46.6333, "São Paulo, SP");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao obter localização: " + e.getMessage());
                    Toast.makeText(getContext(),
                            "Erro ao obter localização. Usando São Paulo.",
                            Toast.LENGTH_SHORT).show();
                    carregarDadosReais(-23.5505, -46.6333, "São Paulo, SP");
                });
    }

    // ---------------- API ----------------

    private void carregarDadosReais(double lat, double lon, String nomeCidade) {
        Log.d(TAG, "carregarDadosReais iniciado para: " + nomeCidade);

        ApiServiceOpenWeather api = ApiOpenWeather.getApiService();

        api.getAirQuality(lat, lon)
                .enqueue(new Callback<AirResponse>() {

                    @Override
                    public void onResponse(
                            Call<AirResponse> call,
                            Response<AirResponse> response
                    ) {
                        Log.d(TAG, "onResponse - Código: " + response.code());

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().list.isEmpty()) {
                            Log.e(TAG, "Resposta sem sucesso: " + response.code());
                            return;
                        }

                        AirResponse data = response.body();

                        int aqi = data.list.get(0).main.aqi;
                        double pm25 = data.list.get(0).components.pm2_5;
                        double pm10 = data.list.get(0).components.pm10;
                        double o3   = data.list.get(0).components.o3;
                        double no2  = data.list.get(0).components.no2;
                        double so2  = data.list.get(0).components.so2;
                        double co   = data.list.get(0).components.co;

                        atualizarUI(aqi, pm25, pm10, o3, no2, so2, co, nomeCidade);
                    }

                    @Override
                    public void onFailure(Call<AirResponse> call,
                                          Throwable t) {
                        Log.e(TAG, "Erro na API: " + t.getMessage());
                        Toast.makeText(getContext(),
                                "Erro ao carregar dados da qualidade do ar",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- Atualização real ----------------

    private void atualizarUI(
            int aqi,
            double pm25,
            double pm10,
            double o3,
            double no2,
            double so2,
            double co,
            String nomeCidade
    ) {
        Log.d(TAG, "=== INICIANDO atualizarUI ===");
        Log.d(TAG, "AQI: " + aqi);
        Log.d(TAG, "Cidade: " + nomeCidade);

        binding.textLocalizacao.setText(nomeCidade);
        binding.textAqiValor.setText(String.valueOf(aqi));
        binding.textAqiStatus.setText(AirQualityUtils.statusAqi(aqi));
        binding.textRecomendacao.setText(
                AirQualityUtils.gerarRecomendacaoAqi(aqi)
        );

        // LIMPA antes de adicionar
        poluentes.clear();
        Log.d(TAG, "Lista limpa. Tamanho: " + poluentes.size());

        poluentes.add(new Poluente(
                "PM2.5",
                "Partículas Finas",
                pm25 + " µg/m³",
                AirQualityUtils.statusPm25(pm25)
        ));

        poluentes.add(new Poluente(
                "PM10",
                "Partículas Inaláveis",
                pm10 + " µg/m³",
                AirQualityUtils.statusPm10(pm10)
        ));

        poluentes.add(new Poluente(
                "O₃",
                "Ozônio",
                o3 + " µg/m³",
                AirQualityUtils.statusO3(o3)
        ));

        poluentes.add(new Poluente(
                "NO₂",
                "Dióxido de Nitrogênio",
                no2 + " µg/m³",
                AirQualityUtils.statusNo2(no2)
        ));

        poluentes.add(new Poluente(
                "SO₂",
                "Dióxido de Enxofre",
                so2 + " µg/m³",
                AirQualityUtils.statusSo2(so2)
        ));

        poluentes.add(new Poluente(
                "CO",
                "Monóxido de Carbono",
                co + " µg/m³",
                AirQualityUtils.statusCo(co)
        ));

        Log.d(TAG, "Poluentes adicionados. Tamanho: " + poluentes.size());
        adapter.notifyDataSetChanged();
        Log.d(TAG, "=== FIM atualizarUI ===");
    }

    // ---------------- Lifecycle ----------------

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}