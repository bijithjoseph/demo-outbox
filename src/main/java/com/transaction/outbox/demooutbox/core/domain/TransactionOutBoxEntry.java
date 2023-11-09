package com.transaction.outbox.demooutbox.core.domain;

import com.transaction.outbox.demooutbox.core.converter.InvocationConverter;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.Instant;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Entity(name = "TRAN_OUTBOX")
public class TransactionOutBoxEntry extends Auditable {

    @NonNull
    @Getter
    @Id
    @Column(name = "TRAN_OUTBOX_ID")
    private String id;

    @Getter
    @Column(name="UNQ_RQST_ID")
    @Setter
    private String uniqueRequestId;

    @Getter
    @Setter
    @Column(name ="APPL")
    private String application;

    @NonNull
    @Getter
    @Setter
    @Column(name = "INVOCTN")
    @Convert(converter = InvocationConverter.class)
    @Lob
    private Invocation invocation;

    @Getter
    @Setter
    @Column(name = "LST_ATTMPT_TS")
    private Instant lastAttemptTime;

    @Getter
    @Setter
    @Column(name = "NXT_ATTMPT_TS")
    private Instant nextAttemptTime;

    @Getter
    @Setter
    @Column(name = "ATTMPT_CNT")
    private int attempts;

    @Getter
    @Setter
    @Column(name = "BLCKD_SW")
    private boolean blocked;

    @Getter
    @Setter
    @Column(name = "PRCSSD_SW")
    private boolean processed;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Transient
    private boolean initialized;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Transient
    private String description;

    public String description(){
        if(!this.initialized){
            synchronized (this){
                if(!this.initialized){
                    this.description = String.format(
                            "%s.%s(%s) [id=%s]uniqueRequestId=%s",
                            invocation.getClassName(),
                            invocation.getMethodName(),
                            invocation.getArgumentHolders(),
                            id,
                            uniqueRequestId == null ? "" : "uid=["+uniqueRequestId+"]");
                    this.initialized = true;
                    return this.description;
                }
            }
        }
        return this.description;
    }
}
