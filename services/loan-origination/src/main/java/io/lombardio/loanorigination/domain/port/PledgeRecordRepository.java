package io.lombardio.loanorigination.domain.port;

import io.lombardio.loanorigination.domain.model.PledgeRecord;

public interface PledgeRecordRepository {

    PledgeRecord save(PledgeRecord pledgeRecord);
}
