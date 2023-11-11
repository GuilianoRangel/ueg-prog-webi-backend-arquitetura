package br.ueg.prog.webi.api.dto;

import br.ueg.prog.webi.api.interfaces.ISearchFieldData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchFieldData implements ISearchFieldData<String> {
    private String id;
    private String description;
    public SearchFieldData(ISearchFieldData<?> data){
        this.id = String.valueOf(data.getId());
        this.description = data.getDescription();
    }
}
