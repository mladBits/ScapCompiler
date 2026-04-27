package com.example.scap.model.parsed.oval;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ParsedOvalObjectSet extends ParsedOvalObjectBase {
    private ParsedOvalSet set;
}
