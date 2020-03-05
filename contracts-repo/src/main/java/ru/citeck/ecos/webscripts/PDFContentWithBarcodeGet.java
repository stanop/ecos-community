package ru.citeck.ecos.webscripts;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.model.ContractsModel;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.server.utils.Utils;
import ru.citeck.ecos.service.PDFBarcodeService;
import ru.citeck.ecos.service.PDFContentTransformService;

import java.io.IOException;

public class PDFContentWithBarcodeGet extends AbstractWebScript {

    // web script arguments
    private static final String PARAM_RECORDREF = "recordRef";
    private static final String PARAM_BARCODETYPE = "barcodeType";

    private static final String TYPE_BARCODE128 = "Barcode128";
    private static final String TYPE_QRCODE = "QRcode";
    private static final QName PROP_BARCODE = QName.createQName(ContractsModel.NAMESPACE, "barcode");

    @Autowired
    private PDFBarcodeService pdfBarcodeService;
    @Autowired
    private PDFContentTransformService pdfContentTransformService;
    @Autowired
    private NodeService nodeService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String recordRefStr = req.getParameter(PARAM_RECORDREF);
        if (recordRefStr == null) {
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }

        String barcodeTypeStr = req.getParameter(PARAM_BARCODETYPE);
        if (barcodeTypeStr == null) {
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }

        NodeRef nodeRef = new NodeRef(recordRefStr);
        if (!nodeService.exists(nodeRef)) {
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }

        DataBundle transformContent = pdfContentTransformService.getTransformContent(nodeRef);
        if (transformContent == null) {
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }

        DataBundle pdfFileWithBarcode = null;
        switch (barcodeTypeStr) {
            case TYPE_BARCODE128:
                String barcode = (String) nodeService.getProperty(nodeRef, PROP_BARCODE);
                if (barcode == null) {
                    res.setStatus(Status.STATUS_BAD_REQUEST);
                    return;
                }
                pdfFileWithBarcode = pdfBarcodeService.putBarcodeOnDocument(transformContent, barcode);
                break;
            case TYPE_QRCODE:
                pdfFileWithBarcode = pdfBarcodeService.putBarcodeOnDocument(transformContent, null, nodeRef.toString());
                break;
            default:
                pdfFileWithBarcode = transformContent;
                break;
        }

        byte[] pdfFileWithBarcodeArray = new byte[pdfFileWithBarcode.getInputStream().available()];
        pdfFileWithBarcode.getInputStream().read(pdfFileWithBarcodeArray);

        res.getOutputStream().write(pdfFileWithBarcodeArray);
        res.setContentType(MimetypeMap.MIMETYPE_PDF);
        res.setHeader("Content-Disposition",
            Utils.encodeContentDispositionForDownload(req, "Document","pdf", false));

    }


    public void setPdfBarcodeService(PDFBarcodeService pdfBarcodeService) {
        this.pdfBarcodeService = pdfBarcodeService;
    }

    public void setPdfContentTransformService(PDFContentTransformService pdfContentTransformService) {
        this.pdfContentTransformService = pdfContentTransformService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

}
