package br.fmu.projetoasthmaspace.Core.Util;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaginaResponse<T> {

    @SerializedName("content")
    private List<T> content;

    @SerializedName("number")
    private int number;

    @SerializedName("size")
    private int size;

    @SerializedName("totalElements")
    private long totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("last")
    private boolean last;

    public List<T> getContent() { return content; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
}
