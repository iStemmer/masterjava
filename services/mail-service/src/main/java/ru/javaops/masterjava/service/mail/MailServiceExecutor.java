package ru.javaops.masterjava.service.mail;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.ExceptionType;
import ru.javaops.masterjava.service.mail.util.MailUtils;
import ru.javaops.masterjava.service.mail.util.MailUtils.MailObject;
import ru.javaops.masterjava.web.WebStateException;
import ru.javaops.masterjava.web.WsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
public class MailServiceExecutor {

    private static final String INTERRUPTED_BY_FAULTS_NUMBER = "+++ Interrupted by faults number";
    private static final String INTERRUPTED_BY_TIMEOUT = "+++ Interrupted by timeout";

    private static final ExecutorService mailExecutor = Executors.newFixedThreadPool(8);

    public static void sendAsync(MailObject mailObject) {
        Set<Addressee> addressees = MailUtils.split(mailObject.getUsers());
        addressees.forEach(addressee ->
                mailExecutor.submit(() -> {
                    try {
                        MailSender.sendTo(addressee, mailObject.getSubject(), mailObject.getBody(),
                                ImmutableList.of(MailUtils.getAttachment(mailObject.getAttachName(), mailObject.getAttachData())));
                    } catch (WebStateException e) {
                        // already logged
                    }
                })
        );
    }

    public static GroupResult sendBulk(final Set<Addressee> addressees, final String subject, final String body, List<Attachment> attachments) throws WebStateException {
        final CompletionService<MailResult> completionService = new ExecutorCompletionService<>(mailExecutor);
        List<Future<MailResult>> futures = StreamEx.of(addressees)
                .map(addressee -> completionService.submit(() -> MailSender.sendTo(addressee, subject, body, attachments)))
                .toList();

        private void cancel(String cause, Throwable t) throws WebStateException {
            futures.forEach(f -> f.cancel(true));
            return new GroupResult(success, failed, cause);
            if (cause != null) {
                throw new WebStateException(cause, ExceptionType.EMAIL);
            } else {
                throw WsClient.getWebStateException(t, ExceptionType.EMAIL);
            }
        }

        return new Callable<GroupResult>() {
            private int success = 0;
            return

            cancelWithFail(INTERRUPTED_EXCEPTION);

            GroupResult groupResult = new GroupResult(success, failed, null);
            log.info("groupResult: {}",groupResult);
            return groupResult;
        }


        private GroupResult cancelWithFail (String cause){
        }
    }.

    call();
}
    }

